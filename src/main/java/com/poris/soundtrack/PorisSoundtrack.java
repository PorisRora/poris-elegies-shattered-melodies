package com.poris.soundtrack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Poris Elegies: Shattered Melodies. Client-only: plays music by its own rules in enabled scenes.
 * It adds no server-synchronised content registries, so servers need not install it; the main mod is optional.
 */
@Mod(value=PorisSoundtrack.MOD_ID,dist=Dist.CLIENT)
public final class PorisSoundtrack {
    public static final String MOD_ID="poris_elegies_shattered_melodies";

    public PorisSoundtrack(IEventBus modBus,ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT,SoundtrackConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class,ConfigurationScreen::new);
        modBus.addListener(SoundtrackClient::registerReloadListener);
        modBus.addListener(SoundtrackClient::registerOverlay);
        modBus.addListener(SoundtrackClient::soundEngineReloaded);
        modBus.addListener(SoundtrackCommands::registerKeys);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,SoundtrackClient::selectMusic);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false,ExternalMusic::requested);
        NeoForge.EVENT_BUS.addListener(ExternalMusic::soundStarted);
        NeoForge.EVENT_BUS.addListener(ExternalMusic::streamStarted);
        NeoForge.EVENT_BUS.addListener(ExternalMusic::loggedOut);
        NeoForge.EVENT_BUS.addListener(SoundtrackCommands::register);
        NeoForge.EVENT_BUS.addListener(SoundtrackCommands::tick);
    }
}
