package com.poris.soundtrack;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;

/** Internal selection state, adapted from Forge's MusicManager tick (Forge 1.20.1 has no selection event). */
final class MusicSelection {
    private final Music original;
    private final SoundInstance playing;
    private Music music;
    private boolean canceled;

    MusicSelection(Music original,SoundInstance playing){this.original=original;this.music=original;this.playing=playing;}
    Music getOriginalMusic(){return original;}
    Music getMusic(){return music;}
    SoundInstance getPlayingMusic(){return playing;}
    boolean isCanceled(){return canceled;}
    void setMusic(Music music){this.music=music;}
    void overrideMusic(Music music){this.music=music;this.canceled=true;}
}
