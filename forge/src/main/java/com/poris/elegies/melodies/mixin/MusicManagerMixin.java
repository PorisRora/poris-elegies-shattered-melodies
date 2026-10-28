package com.poris.elegies.melodies.mixin;

import com.poris.elegies.melodies.SoundtrackClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Null selection is handled before vanilla's non-null-only tick, stopping only its own managed sound. */
@Mixin(MusicManager.class)
abstract class MusicManagerMixin {
    @Shadow private SoundInstance currentMusic;
    @Shadow public abstract void stopPlaying();
    @Unique private Music porisMelodies$selection;

    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void porisMelodies$select(CallbackInfo ci) {
        porisMelodies$selection=SoundtrackClient.selectForTick(Minecraft.getInstance().getSituationalMusic(),currentMusic);
        if(porisMelodies$selection==null) {
            if(currentMusic!=null)stopPlaying();
            ci.cancel();
        }
    }

    @ModifyVariable(method="tick",at=@At("STORE"),ordinal=0)
    private Music porisMelodies$applySelection(Music original) {
        return porisMelodies$selection;
    }
}
