package com.poris.soundtrack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Plays the director's songs as streamed, unpositioned music. Nothing is registered, so servers never need this mod. */
final class MusicPlayer implements Director.Output {
    private Song song;
    private String lastShown;

    static ResourceLocation location(String trackId){return ResourceLocation.fromNamespaceAndPath(PorisSoundtrack.MOD_ID,"music."+trackId);}

    /** Shows the now-playing card, but not again when the same track simply repeats (the dragon fight loops). */
    @Override public void start(TrackList.Track track) {
        stop();
        song=new Song(track);
        var mc=Minecraft.getInstance();
        mc.getSoundManager().play(song);
        if(SoundtrackConfig.nowPlaying() && !track.id().equals(lastShown))mc.getToasts().addToast(new NowPlayingToast(track));
        lastShown=track.id();
    }

    @Override public void level(float level){if(song!=null)song.level=level;}

    @Override public void stop() {
        if(song==null)return;
        song.finish();
        Minecraft.getInstance().getSoundManager().stop(song);
        song=null;
    }

    @Override public boolean playing(){return song!=null && Minecraft.getInstance().getSoundManager().isActive(song);}

    private static final class Song extends AbstractTickableSoundInstance {
        final float base;
        volatile float level;

        /** Starts silent: the director fades every song in, so not even the first audio frame plays at full volume. */
        Song(TrackList.Track track) {
            super(SoundEvent.createVariableRangeEvent(location(track.id())),SoundSource.MUSIC,SoundInstance.createUnseededRandom());
            base=track.volume();
            relative=true;attenuation=Attenuation.NONE;looping=false;delay=0;pitch=1;volume=0;
        }

        @Override public boolean canStartSilent(){return true;}

        /** Fade level from the director; standing in a nether portal fades the music along with the purple swirl. */
        @Override public void tick() {
            var player=Minecraft.getInstance().player;
            volume=base*level*(player==null?1:1-player.spinningEffectIntensity);
        }

        void finish(){stop();}
    }
}
