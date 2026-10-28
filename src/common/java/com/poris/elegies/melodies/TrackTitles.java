package com.poris.elegies.melodies;

import net.minecraft.network.chat.Component;

/** Localized display names. Custom tracks without translations keep their original title. */
final class TrackTitles {
    static Component name(TrackList.Track track) {
        String original=track.titleJa().isEmpty()?track.id():track.titleJa();
        return Component.translatableWithFallback(ShatteredMelodies.MOD_ID+".track."+track.id(),original);
    }

    private TrackTitles(){}
}
