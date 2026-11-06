package com.stew.potlucked.registries;

import com.stew.potlucked.Potlucked;
import com.stew.potlucked.gui.StewPotMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Potlucked.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<StewPotMenu>> STEW_POT_MENU =
            MENU_TYPES.register("stew_pot_menu", () -> IMenuTypeExtension.create(StewPotMenu::new));
}
