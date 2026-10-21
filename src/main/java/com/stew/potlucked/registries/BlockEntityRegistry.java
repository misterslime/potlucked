package com.stew.potlucked.registries;

import com.stew.potlucked.Potlucked;
import com.stew.potlucked.content.StewPotBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

// note: i fucking hate this pattern please rewrite it someday
public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Potlucked.MOD_ID);

    public static final Supplier<BlockEntityType<StewPotBlockEntity>> STEW_POT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "stew_pot_block_entity",
            () -> BlockEntityType.Builder.of(
                    StewPotBlockEntity::new,
                    BlockRegistry.STEW_POT_BLOCK.get()
            ).build(null) // there *isnt* a better way??
    );

}
