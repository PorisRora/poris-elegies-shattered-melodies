package com.poris.soundtrack;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import org.slf4j.Logger;

/** Wires the director into the game: reads the track list, takes over music selection, draws the debug overlay. */
final class SoundtrackClient {
    private static final Logger LOG=LogUtils.getLogger();
    private static final MusicPlayer PLAYER=new MusicPlayer();
    private static final MusicHandoff<SoundInstance> HANDOFF=new MusicHandoff<>();
    private static Director director;
    private static TrackList list=TrackList.EMPTY;
    private static boolean undergroundReady;
    private static boolean firstPartyMusicActive;
    private static String handoffReason="",handoffDetail="",lastHandoffTrace="";

    static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener)SoundtrackClient::reload);
    }

    /** Runs after the sound manager has reloaded, so tracks without audio can be left out. */
    private static void reload(ResourceManager resources) {
        PLAYER.stop();
        HANDOFF.reset();
        firstPartyMusicActive=false;
        handoffReason=handoffDetail=lastHandoffTrace="";
        var sounds=Minecraft.getInstance().getSoundManager();
        try(Reader tracks=resources.openAsReader(file("tracks.json"));
            Reader lengths=resources.getResource(file("lengths.json")).isPresent()?resources.openAsReader(file("lengths.json")):null) {
            list=TrackList.parse(tracks,lengths,id->sounds.getSoundEvent(MusicPlayer.location(id))!=null);
        } catch(IOException|RuntimeException e) {
            LOG.error("Could not read the soundtrack list; background music stays silent",e);
            list=TrackList.EMPTY;
        }
        director=new Director(list,RandomGenerator.getDefault(),PLAYER);
        var underground=list.pool(MusicContext.UNDERGROUND);
        undergroundReady=underground.key().equals(MusicContext.UNDERGROUND) && !underground.ids().isEmpty();
        LOG.info("Soundtrack: {} tracks with audio for {} music events",list.tracks().size(),list.pools().size());
    }

    private static ResourceLocation file(String name){return ResourceLocation.fromNamespaceAndPath(PorisSoundtrack.MOD_ID,"soundtrack/"+name);}

    /**
     * Listens last, so both selection overrides and direct audio players obey the configured conflict policy.
     * Disabled scenes pass through unchanged. Our player never depends on MusicManager owning its sound.
     */
    static void selectMusic(SelectMusicEvent event) {
        if(director==null)return;
        var mc=Minecraft.getInstance();
        Music original=event.getOriginalMusic(),chosen=event.getMusic();
        String originalContext=original.getEvent().value().getLocation().toString();
        boolean enabled=MusicContext.enabled(originalContext,SoundtrackConfig.menuMusic(),SoundtrackConfig.inGameMusic());
        director.setEnabled(enabled);
        var policy=SoundtrackConfig.musicPolicy(enabled);
        ExternalMusic.updatePolicy(policy);
        boolean claimed=ExternalMusic.tickClaims();
        firstPartyMusicActive=claimed;
        ExternalMusic.enforcePolicy();
        String dimension=mc.level==null?null:mc.level.dimension().location().toString();
        Music biome=mc.player!=null && MusicContext.CREATIVE.equals(originalContext) && MusicContext.OVERWORLD.equals(dimension)
                ?mc.player.level().getBiome(mc.player.blockPosition()).value().getBackgroundMusic().orElse(null):null;
        String context=MusicContext.resolve(originalContext,dimension,biome==null?null:biome.getEvent().value().getLocation().toString());
        if(mc.player!=null) {   // ordinary caves play our underground scene (BGM design 6.4 rule 7)
            var level=mc.player.level();
            var pos=mc.player.blockPosition();
            int depth=level.getHeight(Heightmap.Types.WORLD_SURFACE,pos.getX(),pos.getZ())-pos.getY();
            context=MusicContext.underground(context,dimension,depth,level.getBrightness(LightLayer.SKY,pos),undergroundReady);
        }
        boolean foreign=!context.startsWith("minecraft:") && !list.pools().containsKey(context);
        // A modded biome may supply its own music, hidden by vanilla's creative priority. Let that music through.
        if(enabled && foreign && biome!=null && !context.equals(originalContext) && chosen==original && !event.isCanceled()) {
            event.setMusic(biome);chosen=biome;
        }
        var selectionYield=event.isCanceled() || foreign || chosen!=null && chosen!=original?Director.Yield.OTHER
                :chosen==null?Director.Yield.SILENCE:Director.Yield.NONE;
        String chosenId=chosen==null?null:chosen.getEvent().value().getLocation().toString();
        selectionYield=policy.selectionYield(selectionYield,chosenId);
        var managed=event.getPlayingMusic();
        HANDOFF.observe(managed,managed==null?null:managed.getLocation().toString(),selectionYield!=Director.Yield.NONE,
                chosenId);
        if(HANDOFF.externalManaged() && (ExternalMusic.blocked(managed) || !ExternalMusic.playing(managed)
                || mc.options.getSoundSourceVolume(SoundSource.MASTER)<=0
                || mc.options.getSoundSourceVolume(managed.getSource())<=0))HANDOFF.managedEnded();
        boolean occupied=ExternalMusic.tick(HANDOFF) || claimed;
        // Disabling our BGM restores native music; a direct spell reservation still silences it for the song's duration.
        if(!enabled){if(claimed)event.setMusic(null);return;}
        var decision=claimed?new MusicHandoff.Decision(Director.Yield.OTHER,MusicHandoff.Action.SUPPRESS_VANILLA)
                :MusicHandoff.decide(selectionYield,occupied,HANDOFF.externalManaged(),policy.overlapsExternal());
        updateHandoffReason(decision,selectionYield,chosenId,managed,policy,context);
        var frequency=SoundtrackConfig.frequency();
        boolean audible=mc.options.getSoundSourceVolume(SoundSource.MUSIC)>0 && mc.options.getSoundSourceVolume(SoundSource.MASTER)>0;
        director.tick(new Director.Situation(context,dimension,
                decision.yield(),audible,frequency.min,frequency.max));
        switch(decision.action()) {
            case SUPPRESS_VANILLA -> event.setMusic(null);
            case KEEP_EXTERNAL -> event.setMusic(new Music(Holder.direct(SoundEvent.createVariableRangeEvent(managed.getLocation())),
                    Integer.MAX_VALUE,Integer.MAX_VALUE,false));
            case PASS -> {}
        }
    }

    static void soundEngineReloaded(SoundEngineLoadEvent event) {
        ExternalMusic.engineLoaded(event.getEngine());
        if(director!=null) {
            Minecraft.getInstance().execute(()->{HANDOFF.reset();firstPartyMusicActive=false;director.reset();});
        }
    }

    /** For /bgm and its keys; null until the first resource load. */
    static Director director(){return director;}
    static TrackList tracks(){return list;}
    static boolean firstPartyMusicActive(){return firstPartyMusicActive;}

    private static void updateHandoffReason(MusicHandoff.Decision decision,Director.Yield selectionYield,
                                           String chosenId,SoundInstance managed,MusicPolicy policy,String context) {
        String reason="",detail="";
        if(decision.yield()!=Director.Yield.NONE) {
            if(firstPartyMusicActive)reason="first_party_yielded";
            else if(selectionYield==Director.Yield.SILENCE)reason="silence_requested";
            else if(selectionYield!=Director.Yield.NONE){reason="selection_yielded";detail=chosenId==null?"-":chosenId;}
            else if(ExternalMusic.audibleCount()>0)reason="yielded";
            else if(HANDOFF.externalManaged()){reason="managed_yielded";detail=managed.getLocation().toString();}
            else if(ExternalMusic.graceTicks()>0)reason="buffering";
        }
        handoffReason=reason;handoffDetail=detail;
        String trace=policy.mode()+"/"+reason+"/"+detail;
        if(!trace.equals(lastHandoffTrace)) {
            LOG.info("Music handoff: mode={}, context={}, reason={}, selection={}, managed={}, external={}, graceTicks={}, channels={}",
                    policy.mode(),context,reason.isEmpty()?"none":reason,chosenId,
                    managed==null?"none":managed.getLocation(),ExternalMusic.audibleCount(),ExternalMusic.graceTicks(),
                    ExternalMusic.describeChannels(HANDOFF));
            lastHandoffTrace=trace;
        }
    }

    static Component yieldStatus() {
        String reason=firstPartyMusicActive?"first_party_yielded":handoffReason.isEmpty()?"resuming":handoffReason;
        return Component.translatable(PorisSoundtrack.MOD_ID+".debug."+reason,
                reason.equals("buffering")?remainingClock(ExternalMusic.graceTicks()):handoffDetail);
    }

    static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(PorisSoundtrack.MOD_ID,"debug"),SoundtrackClient::drawOverlay);
    }

    private static void drawOverlay(GuiGraphics graphics,DeltaTracker delta) {
        var mc=Minecraft.getInstance();
        if(director==null || !SoundtrackConfig.debugOverlay() || mc.options.hideGui || mc.gui.getDebugOverlay().showDebugScreen())return;
        var lines=lines(director.status());
        int width=0;
        for(var line:lines)width=Math.max(width,mc.font.width(line));
        graphics.fill(2,2,8+width,6+lines.size()*10,0x90000000);
        for(int i=0;i<lines.size();i++)graphics.drawString(mc.font,lines.get(i),5,5+i*10,0xFFFFFF,false);
    }

    private static List<Component> lines(Director.Status s) {
        String key=PorisSoundtrack.MOD_ID+".debug.";
        var lines=new ArrayList<Component>();
        var t=s.track();
        lines.add(t==null?Component.translatable(key+(firstPartyMusicActive?"first_party":"none")):Component.translatable(key+"track",TrackTitles.name(t),
                Component.translatable(PorisSoundtrack.MOD_ID+".energy."+t.energy().key()),clock(s.playedTicks()),t.seconds()>0?clock(t.seconds()*20):"?"));
        lines.add(switch(s.phase()) {
            case IDLE -> Component.translatable(key+"idle",clock(Math.max(0,s.delay())));
            case PLAYING -> Component.translatable(key+(s.requested()?"requested":"playing"));
            case FADING -> Component.translatable(key+"fading");
            case BREATH -> Component.translatable(key+"breath");
            case YIELDED -> yieldStatus();
            case DISABLED -> Component.translatable(key+"disabled");
        });
        lines.add(Component.translatable(key+"place",s.context()==null?"-":s.context().replace("minecraft:music.","").replace(PorisSoundtrack.MOD_ID+":",""),s.here()/20,s.cooldown()/20));
        if(ExternalMusic.audibleCount()>0 || ExternalMusic.graceTicks()>0)
            lines.add(Component.translatable(key+"external",ExternalMusic.audibleCount(),remainingClock(ExternalMusic.graceTicks())));
        return lines;
    }

    static String clock(int ticks){int seconds=ticks/20;return seconds/60+":"+String.format("%02d",seconds%60);}
    static String remainingClock(int ticks){return clock(Math.max(0,ticks)+19);}

    private SoundtrackClient(){}
}
