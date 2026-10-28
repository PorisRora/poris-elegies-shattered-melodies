package com.poris.elegies.melodies;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.ConfigScreenHandler;

/** The client bootstrap is isolated so an accidental server installation cannot load client classes. */
@Mod(ShatteredMelodies.MOD_ID)
public final class ShatteredMelodies {
    public static final String MOD_ID="poris_elegies_shattered_melodies";

    public ShatteredMelodies() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT,()->Client::init);
    }

    private static final class Client {
        static void init() {
            var loading=ModLoadingContext.get();
            loading.registerConfig(ModConfig.Type.CLIENT,SoundtrackConfig.SPEC);
            loading.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    ()->new ConfigScreenHandler.ConfigScreenFactory((mc,parent)->new SoundtrackConfigScreen(parent)));
            var modBus=FMLJavaModLoadingContext.get().getModEventBus();
            modBus.addListener(SoundtrackClient::registerReloadListener);
            modBus.addListener(SoundtrackClient::registerOverlay);
            modBus.addListener(SoundtrackClient::soundEngineReloaded);
            modBus.addListener(SoundtrackCommands::registerKeys);
            var bus=MinecraftForge.EVENT_BUS;
            bus.addListener(EventPriority.LOWEST,false,ExternalMusic::requested);
            bus.addListener(ExternalMusic::soundStarted);
            bus.addListener(ExternalMusic::streamStarted);
            bus.addListener(ExternalMusic::loggedOut);
            bus.addListener(SoundtrackCommands::register);
            bus.addListener(SoundtrackCommands::tick);
        }
    }
}
