package com.stew.potlucked.client;

import com.stew.potlucked.Potlucked;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Potlucked.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Potlucked.MOD_ID, value = Dist.CLIENT)
public class PotluckedClient {
    public PotluckedClient(ModContainer container) {
        // oh configuration
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Potlucked.LOGGER.info("HELLO FROM CLIENT SETUP");
        Potlucked.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
