package com.poris.elegies.melodies;

import java.util.Objects;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/** Direct client API for Poris-owned spell/story songs. The soundtrack itself does not depend on the main mod. */
public final class MusicControl {
    public interface Lease extends AutoCloseable {
        /** Idempotent; release on cancel, end, dimension change or leaving hearing range. Does not stop the sound. */
        @Override void close();
    }

    /**
     * Call on the client thread before SoundManager.play. The exact instance is allowed in every mode and reserves
     * the BGM scene during loading. Keep the returned lease until the song ends, then close it.
     * Only Poris main-mod sound events can use this priority; external music follows the user's selected mode.
     */
    public static Lease acquire(SoundInstance sound) {
        Objects.requireNonNull(sound,"sound");
        if(!sound.getLocation().getNamespace().equals("poris_elegies")
                || sound.getSource()!=SoundSource.MUSIC && sound.getSource()!=SoundSource.RECORDS)
            throw new IllegalArgumentException("The Poris music API requires a poris_elegies MUSIC or RECORDS instance");
        var release=ExternalMusic.claim(sound);
        return release::run;
    }

    private MusicControl(){}
}
