package com.stew.potlucked.content;

import com.stew.potlucked.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

public class StewPotBlockEntity extends BlockEntity {
    public static final int BOWL_SLOT = 0;
    public static final int SHOVEL_STICK_SLOT = 1;

    // Contains ingredients and inputs into the recipe.
    public final ItemStackHandler inventoryHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Contains bowl output and shovel/stick stir input.
    public final ItemStackHandler toolsHandler = new ItemStackHandler(2) {
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case BOWL_SLOT -> stack.is(Items.BOWL);
                case SHOVEL_STICK_SLOT -> stack.is(ItemTags.SHOVELS) || stack.is(Items.STICK);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final CombinedInvWrapper combinedHandler = new CombinedInvWrapper(inventoryHandler, toolsHandler);

    // Contains water for the pot block.
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

            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
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

        if (tag.contains("inventoryHandler"))
            this.inventoryHandler.deserializeNBT(registries, tag.getCompound("inventoryHandler"));

        if (tag.contains("toolsHandler"))
            this.toolsHandler.deserializeNBT(registries, tag.getCompound("toolsHandler"));
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.put("inventoryHandler", inventoryHandler.serializeNBT(registries));
        tag.put("toolsHandler", toolsHandler.serializeNBT(registries));
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

    // returns true if either tool insertion or ingredient insertion succeeds or if you can get the bowl
    // from the bowl slot.
    public boolean tryInteract(@NotNull BlockPos pos, @NotNull Player player, @NotNull ItemStack stack) {
        if (tryInsertTool(pos, player, stack))
            return true;

        if (tryInsertIngredient(pos, player, stack))
            return true;

        if (!stack.isEmpty() || toolsHandler.getStackInSlot(BOWL_SLOT).isEmpty())
            return false;

        ItemStack bowl = toolsHandler.extractItem(BOWL_SLOT, 1, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, bowl);
        toolsHandler.setStackInSlot(BOWL_SLOT, ItemStack.EMPTY);
        assert this.level != null;
        this.level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);

        return true;
    }

    // returns true if the item stack was successfully inserted into one of the tool slots
    // tools can be inserted if they're bowls, shovels, or sticks.
    public boolean tryInsertTool(@NotNull BlockPos pos, @NotNull Player player, @NotNull ItemStack stack) {
        if (stack.isEmpty())
            return false;

        if (stack.is(Items.BOWL) &&
                toolsHandler.getStackInSlot(BOWL_SLOT).isEmpty())
        {
            toolsHandler.insertItem(BOWL_SLOT, stack.copy(), false);
            stack.shrink(1);
            assert this.level != null;
            this.level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            return true;
        }
        else if ((stack.is(ItemTags.SHOVELS) || stack.is(Items.STICK)) &&
                toolsHandler.getStackInSlot(SHOVEL_STICK_SLOT).isEmpty())
        {
            toolsHandler.insertItem(SHOVEL_STICK_SLOT, stack.copy(), false);
            stack.shrink(1);
            assert this.level != null;
            this.level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            return true;
        }

        return false;
    }

    // returns true if the item stack was successfully inserted into one of the ingredient inventory slots
    public boolean tryInsertIngredient(@NotNull BlockPos pos, @NotNull Player player, @NotNull ItemStack stack) {
        if (stack.isEmpty())
            return false;

        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            if (!inventoryHandler.getStackInSlot(i).isEmpty())
                continue;

            inventoryHandler.insertItem(i, stack.copy(), false);
            stack.shrink(1);
            assert this.level != null;
            this.level.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 2f);

            return true;
        }

        return false;
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(combinedHandler.getSlots());

        for (int i = 0; i < combinedHandler.getSlots(); i++) {
            inv.setItem(i, combinedHandler.getStackInSlot(i));
        }

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inv);
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
}
