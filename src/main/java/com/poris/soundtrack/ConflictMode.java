package com.poris.soundtrack;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

/** Whether external music owns the scene or must be explicitly allowed. */
public enum ConflictMode implements TranslatableEnum {
    NORMAL, YIELD, EXCLUSIVE, WHITELIST;

    @Override public Component getTranslatedName() {
        return Component.translatable(PorisSoundtrack.MOD_ID+".configuration.conflictMode."+name().toLowerCase(Locale.ROOT));
    }
}
