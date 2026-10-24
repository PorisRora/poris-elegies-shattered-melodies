package com.poris.soundtrack;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Gap between two tracks, in ticks. SPARSE is the vanilla pace. */
public enum Frequency {
    SPARSE(12000,24000), STANDARD(4800,9600), FREQUENT(1200,3600);

    final int min,max;
    Frequency(int min,int max){this.min=min;this.max=max;}

    public Component getTranslatedName() {
        return Component.translatable(PorisSoundtrack.MOD_ID+".configuration.frequency."+name().toLowerCase(Locale.ROOT));
    }
}
