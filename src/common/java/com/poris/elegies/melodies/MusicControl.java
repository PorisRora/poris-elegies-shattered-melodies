package com.poris.elegies.melodies;

import java.util.Objects;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/** Direct client API for spell/story songs that take over the soundtrack. The soundtrack itself does not depend on any mod. */
public final class MusicControl {
    public interface Lease extends AutoCloseable {
        /** Idempotent; release on cancel, end, dimension change or leaving hearing range. Does not stop the sound. */
        @Override void close();
    }

    /**
     * Call on the client thread before SoundManager.play. The exact instance is allowed in every mode and reserves
     * the BGM scene during loading. Keep the returned lease until the song ends, then close it.
     * Any mod's MUSIC or RECORDS sound can take this priority by calling here.
     * Music played without a lease follows the user's selected mode.
     */
    public static Lease acquire(SoundInstance sound) {
        Objects.requireNonNull(sound,"sound");
        if(sound.getSource()!=SoundSource.MUSIC && sound.getSource()!=SoundSource.RECORDS)
            throw new IllegalArgumentException("Music coordination requires a MUSIC or RECORDS sound instance");
        var release=ExternalMusic.claim(sound);
        return release::run;
    }

    private MusicControl(){}
}
