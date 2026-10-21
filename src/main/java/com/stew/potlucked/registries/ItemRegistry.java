package com.stew.potlucked.registries;

import com.stew.potlucked.Potlucked;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Potlucked.MOD_ID);

    public static final DeferredItem<BlockItem> STEW_POT_ITEM = ITEMS.registerSimpleBlockItem("stew_pot", BlockRegistry.STEW_POT_BLOCK);
}
