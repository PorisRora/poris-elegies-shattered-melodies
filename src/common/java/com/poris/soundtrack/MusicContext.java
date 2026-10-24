package com.poris.soundtrack;

import java.util.Set;

/** Creative overworld exploration uses biome music; menu, underwater, boss and dimension choices keep priority. */
final class MusicContext {
    static final String CREATIVE="minecraft:music.creative",GAME="minecraft:music.game",OVERWORLD="minecraft:overworld";
    /** Our own scene for ordinary caves, where vanilla simply plays the surface biome's music. */
    static final String UNDERGROUND="poris_elegies_shattered_melodies:underground";
    static final int UNDERGROUND_DEPTH=12;
    private static final Set<String> CAVE_BIOMES=Set.of("minecraft:music.overworld.lush_caves","minecraft:music.overworld.dripstone_caves",Director.DEEP_DARK);

    /** Classify by the music context: pausing in a world is still in-game, and credits are not title music. */
    static boolean enabled(String original,boolean menuMusic,boolean inGameMusic) {
        return Director.MENU.equals(original)?menuMusic:inGameMusic;
    }

    static String resolve(String original,String dimension,String biome) {
        if(CREATIVE.equals(original) && OVERWORLD.equals(dimension) && biome!=null && !GAME.equals(biome))return biome;
        // The generic creative pool keeps the building theme in plains and other places without biome music.
        return original;
    }

    /**
     * Ordinary overworld music becomes the underground scene where at least {@link #UNDERGROUND_DEPTH} blocks lie overhead
     * and no daylight reaches. Height alone is not enough (a tunnel inside a mountain is above sea level, a ravine floor is
     * open to the sky), and darkness alone would catch a windowless room at home. Cave biomes and water keep their own music;
     * until the underground pool has audio, the surface music stays.
     */
    static String underground(String context,String dimension,int depth,int skyLight,boolean poolReady) {
        if(!poolReady || !OVERWORLD.equals(dimension) || depth<UNDERGROUND_DEPTH || skyLight>0)return context;
        boolean surface=GAME.equals(context) || CREATIVE.equals(context)
                || context.startsWith("minecraft:music.overworld.") && !CAVE_BIOMES.contains(context);
        return surface?UNDERGROUND:context;
    }

    private MusicContext(){}
}
