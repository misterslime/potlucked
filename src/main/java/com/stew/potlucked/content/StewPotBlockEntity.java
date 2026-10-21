package com.stew.potlucked.content;

import com.stew.potlucked.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class StewPotBlockEntity extends BlockEntity {
    public final FluidTank fluidTank = new FluidTank(1000, stack -> stack.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            if (level == null)
                return;

            BlockState state = getBlockState();

            if (fluid.isEmpty() && state.getValue(StewPotBlock.FILLED)) {
                BlockState newState = state.setValue(StewPotBlock.FILLED, false);
                level.setBlock(getBlockPos(), newState, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
            }

            if (!fluid.isEmpty() && !state.getValue(StewPotBlock.FILLED)) {
                BlockState newState = state.setValue(StewPotBlock.FILLED, true);
                level.setBlock(getBlockPos(), newState, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
            }

            setChanged();
        }
    };

    // the constructor is pascal case and the methods are camel case i hate my life
    public StewPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.STEW_POT_BLOCK_ENTITY.get(), pos, state);
    }

    // Read values from the passed CompoundTag here.
    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("fluidTank"))
            this.fluidTank.readFromNBT(registries, tag.getCompound("fluidTank"));
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StewPotBlockEntity blockEntity) {
        // Ensure this only runs on the server
        if (level.isClientSide())
            return;

        BlockPos blockPosBelow = pos.below();
        BlockState blockStateBelow = level.getBlockState(blockPosBelow);

        boolean isBelowBlockFireOrCampfire = blockStateBelow.is(Blocks.FIRE) ||
                blockStateBelow.is(Blocks.SOUL_FIRE) || CampfireBlock.isLitCampfire(blockStateBelow);

        if (isBelowBlockFireOrCampfire && !state.getValue(StewPotBlock.HEATED)) {
            BlockState newState = state.setValue(StewPotBlock.HEATED, true);
            level.setBlock(pos, newState, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
        }

        if (!isBelowBlockFireOrCampfire && state.getValue(StewPotBlock.HEATED)) {
            BlockState newState = state.setValue(StewPotBlock.HEATED, false);
            level.setBlock(pos, newState, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
