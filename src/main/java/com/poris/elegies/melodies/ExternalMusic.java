package com.poris.elegies.melodies;

import com.poris.elegies.melodies.mixin.SoundEngineAccessor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import net.neoforged.neoforge.client.event.sound.SoundEvent.SoundSourceEvent;

/** Confirmed playback drives handoff. Exclusive mode blocks only the configured music categories. */
final class ExternalMusic {
    private static final ExternalMusicTracker<SoundInstance> TRACKER=new ExternalMusicTracker<>();
    private static final MusicClaims<SoundInstance> CLAIMS=new MusicClaims<>();
    private static volatile SoundEngine engine;
    private static volatile MusicPolicy policy=MusicPolicy.DISABLED;

    static void updatePolicy(MusicPolicy next){policy=next;}
    static Runnable claim(SoundInstance sound){return CLAIMS.acquire(sound);}

    /** Last request filter, before decoding or allocating a channel. Safe even when a caller is off-thread. */
    static void requested(PlaySoundEvent event) {
        var sound=event.getSound();
        if(sound!=null && blocked(sound))event.setSound(null);
    }

    static boolean blocked(SoundInstance sound) {
        return sound!=null && !CLAIMS.contains(sound) && policy.blocks(sound.getLocation().toString(),sound.getSource().name());
    }

    static void soundStarted(PlaySoundSourceEvent event){started(event);}
    static void streamStarted(PlayStreamingSourceEvent event){started(event);}

    private static void started(SoundSourceEvent event) {
        var sound=event.getSound();
        // These events execute on the sound executor. No world, config, Director or SoundManager access here.
        if(engine==null)engine=event.getEngine();
        if(engine!=event.getEngine())return;
        CLAIMS.started(sound);
        if(MusicHandoff.accepts(sound.getLocation().toString(),sound.getSource().name()))TRACKER.started(sound);
    }

    static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event){policy=MusicPolicy.DISABLED;invalidate();}
    static void invalidate(){TRACKER.invalidate();CLAIMS.clear();}
    static void engineLoaded(SoundEngine loaded){engine=loaded;invalidate();}

    /** Client thread only. A map entry must still own a live channel, not just a stale deletion deadline. */
    static boolean playing(SoundInstance sound) {
        var current=engine;
        if(current==null || sound==null || sound instanceof TickableSoundInstance ticking && ticking.isStopped())return false;
        var channel=((SoundEngineAccessor)current).porisMelodies$getChannels().get(sound);
        return channel!=null && !channel.isStopped();
    }

    /** Covers live config changes and requests made before exclusive mode was enabled. Client thread only. */
    static void enforcePolicy() {
        var current=engine;
        if(current==null || !policy.restrictsExternal())return;
        var manager=Minecraft.getInstance().getSoundManager();
        for(var sound:List.copyOf(((SoundEngineAccessor)current).porisMelodies$getChannels().keySet()))
            if(blocked(sound))manager.stop(sound);
    }

    /** Direct Poris reservations take priority even with an empty whitelist, including while the stream is loading. */
    static boolean tickClaims() {
        var mc=Minecraft.getInstance();
        return CLAIMS.tick(ExternalMusic::playing,sound->sound instanceof TickableSoundInstance ticking && ticking.isStopped()
                || mc.options.getSoundSourceVolume(SoundSource.MASTER)<=0
                || mc.options.getSoundSourceVolume(sound.getSource())<=0);
    }

    /** Called even while our scene is disabled, so enabling it during a boss theme cannot start a second song. */
    static boolean tick(MusicHandoff<SoundInstance> handoff) {
        var mc=Minecraft.getInstance();
        var manager=mc.getSoundManager();
        var listener=manager.getListenerTransform().position();
        return TRACKER.tick(sound->{
            if(blocked(sound) || handoff.vanillaManaged(sound) || !playing(sound))return ExternalMusicTracker.State.ENDED;
            if(CLAIMS.contains(sound))return ExternalMusicTracker.State.OWNED;
            // A zero category/master volume stops the channel; do not retain it as a distant/silent source.
            if(mc.options.getSoundSourceVolume(SoundSource.MASTER)<=0
                    || mc.options.getSoundSourceVolume(sound.getSource())<=0)return ExternalMusicTracker.State.ENDED;
            double radius=Math.max(1,sound.getVolume())*sound.getSound().getAttenuationDistance();
            double distance=listener.distanceToSqr(new Vec3(sound.getX(),sound.getY(),sound.getZ()));
            boolean audible=sound.getVolume()>0 && MusicHandoff.inRange(sound.isRelative(),
                    sound.getAttenuation()==SoundInstance.Attenuation.LINEAR,distance,radius);
            return audible?ExternalMusicTracker.State.AUDIBLE:ExternalMusicTracker.State.SILENT;
        });
    }

    static int audibleCount(){return TRACKER.audibleCount();}
    static int graceTicks(){return TRACKER.graceTicks();}

    /** Transition-only diagnostics; never poll or log every sound each frame. */
    static List<String> describeChannels(MusicHandoff<SoundInstance> handoff) {
        var current=engine;
        if(current==null)return List.of();
        return ((SoundEngineAccessor)current).porisMelodies$getChannels().keySet().stream()
                .filter(s->MusicHandoff.accepts(s.getLocation().toString(),s.getSource().name())
                        && !handoff.vanillaManaged(s) && !CLAIMS.contains(s) && playing(s))
                .limit(8).map(s->s.getLocation()+"["+s.getSource()+",volume="+s.getVolume()+"]").toList();
    }

    private ExternalMusic(){}
}
