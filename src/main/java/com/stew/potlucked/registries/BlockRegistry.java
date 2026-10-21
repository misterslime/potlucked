package com.stew.potlucked.registries;

import com.stew.potlucked.Potlucked;
import com.stew.potlucked.content.StewPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Potlucked.MOD_ID);

    public static final DeferredBlock<StewPotBlock> STEW_POT_BLOCK =
            BLOCKS.register("stew_pot", () -> new StewPotBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
}
