package com.stew.potlucked.registries;

import com.stew.potlucked.Potlucked;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabRegistry {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "potlucked" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Potlucked.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> POTLUCKED_TAB = CREATIVE_MODE_TABS.register("potlucked_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.potlucked")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistry.STEW_POT_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.STEW_POT_ITEM.get());
            }).build());

}
