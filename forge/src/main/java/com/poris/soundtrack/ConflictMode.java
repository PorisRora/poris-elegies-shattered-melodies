package com.poris.soundtrack;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Whether external music owns the scene or must be explicitly allowed. */
public enum ConflictMode {
    NORMAL, YIELD, EXCLUSIVE, WHITELIST;

    public Component getTranslatedName() {
        return Component.translatable(PorisSoundtrack.MOD_ID+".configuration.conflictMode."+name().toLowerCase(Locale.ROOT));
    }
}
