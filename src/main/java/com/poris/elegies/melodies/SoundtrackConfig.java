package com.poris.elegies.melodies;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client settings, editable from the mod list's config screen. */
public final class SoundtrackConfig {
    public static final ModConfigSpec SPEC;
    static final ModConfigSpec.BooleanValue MENU_MUSIC;
    static final ModConfigSpec.BooleanValue IN_GAME_MUSIC;
    static final ModConfigSpec.EnumValue<Frequency> FREQUENCY;
    static final ModConfigSpec.EnumValue<ConflictMode> CONFLICT_MODE;
    static final ModConfigSpec.ConfigValue<List<? extends String>> MUSIC_WHITELIST;
    static final ModConfigSpec.BooleanValue NOW_PLAYING;
    static final ModConfigSpec.BooleanValue DEBUG_OVERLAY;

    static {
        var b=new ModConfigSpec.Builder();
        MENU_MUSIC=b.comment("Play the Poris soundtrack on the title screen. Disable to restore vanilla menu music.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.menuMusic")
                .define("menuMusic",true);
        IN_GAME_MUSIC=b.comment("Play the Poris soundtrack in worlds, including creative mode and the End credits.",
                        "Disable to restore vanilla in-game music. Does not change other mods' independent music.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.inGameMusic")
                .define("inGameMusic",true);
        FREQUENCY=b.comment("How often background music plays: SPARSE is the vanilla pace (10-20 min between tracks),",
                        "STANDARD 4-8 min, FREQUENT 1-3 min.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.frequency")
                .defineEnum("frequency",Frequency.FREQUENT);
        CONFLICT_MODE=b.comment("NORMAL (default) allows our BGM and external MUSIC or RECORDS to overlap.",
                        "YIELD fades our music out while external MUSIC or RECORDS are audible.",
                        "EXCLUSIVE blocks all external MUSIC and RECORDS. WHITELIST allows only the list below.",
                        "Vanilla note blocks are always allowed.",
                        "Only applies in scenes where this soundtrack is enabled. Other sound categories are untouched.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.conflictMode")
                .defineEnum("conflictMode",ConflictMode.NORMAL);
        MUSIC_WHITELIST=b.comment("External music allowed in WHITELIST mode. We yield while an allowed track plays.",
                        "Use exact sound event IDs (netmusic:net_music) or a namespace wildcard (cataclysm:*).",
                        "Ignored in NORMAL, YIELD and EXCLUSIVE modes.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.musicWhitelist")
                .defineListAllowEmpty("musicWhitelist",MusicPolicy.DEFAULT_WHITELIST,()->"cataclysm:*",MusicPolicy::validEntry);
        NOW_PLAYING=b.comment("Show a small card with the title in the top-right corner when a new track starts.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.nowPlaying")
                .define("nowPlaying",true);
        DEBUG_OVERLAY=b.comment("Show in the top-left corner what the soundtrack is doing and why.")
                .translation(ShatteredMelodies.MOD_ID+".configuration.debugOverlay")
                .define("debugOverlay",false);
        SPEC=b.build();
    }

    static Frequency frequency() {
        try{return FREQUENCY.get();}catch(IllegalStateException notLoadedYet){return Frequency.FREQUENT;}
    }

    static MusicPolicy musicPolicy(boolean enabled) {
        try{return new MusicPolicy(enabled,MusicPolicy.Mode.valueOf(CONFLICT_MODE.get().name()),List.copyOf(MUSIC_WHITELIST.get()));}
        catch(IllegalStateException notLoadedYet){return new MusicPolicy(enabled,MusicPolicy.Mode.NORMAL,MusicPolicy.DEFAULT_WHITELIST);}
    }

    static boolean menuMusic() {
        try{return MENU_MUSIC.get();}catch(IllegalStateException notLoadedYet){return true;}
    }

    static boolean inGameMusic() {
        try{return IN_GAME_MUSIC.get();}catch(IllegalStateException notLoadedYet){return true;}
    }

    static boolean nowPlaying() {
        try{return NOW_PLAYING.get();}catch(IllegalStateException notLoadedYet){return true;}
    }

    static boolean debugOverlay() {
        try{return DEBUG_OVERLAY.get();}catch(IllegalStateException notLoadedYet){return false;}
    }

    private SoundtrackConfig(){}
}
