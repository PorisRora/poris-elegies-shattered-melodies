package com.poris.elegies.melodies.mixin;

import com.mojang.blaze3d.audio.Listener;
import java.util.Map;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only channel liveness. SoundEngine.isActive can keep stale soundDeleteTime entries after muting a category. */
@Mixin(SoundEngine.class)
public interface SoundEngineAccessor {
    @Accessor("listener")
    Listener porisMelodies$getListener();

    @Accessor("instanceToChannel")
    Map<SoundInstance,ChannelAccess.ChannelHandle> porisMelodies$getChannels();
}
