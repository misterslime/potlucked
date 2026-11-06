package com.stew.potlucked.content;

import com.mojang.serialization.MapCodec;
import com.stew.potlucked.gui.StewPotMenu;
import com.stew.potlucked.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StewPotBlock extends Block implements EntityBlock {
    public static final MapCodec<StewPotBlock> CODEC = simpleCodec(StewPotBlock::new);

    public static final BooleanProperty HEATED = BooleanProperty.create("heated");
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    // i dont currently understand this java notation but it seems to basically be C# where keyword
    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        // ?????? scary wtf why does java have no way to handle this
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    public StewPotBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(stateDefinition.any()
                .setValue(HEATED, false)
                .setValue(FILLED, false)
        );
    }

    // theres so many thigns u need to override i hate java and oop
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HEATED, FILLED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        return defaultBlockState();
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new StewPotBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type
    ) {
        return createTickerHelper(type, BlockEntityRegistry.STEW_POT_BLOCK_ENTITY.get(), StewPotBlockEntity::tick);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level,
                            @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock() &&
                level.getBlockEntity(pos) instanceof StewPotBlockEntity stewPotBlockEntity) {
            stewPotBlockEntity.drops();
            level.updateNeighbourForOutputSignal(pos, this);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof StewPotBlockEntity stewPotBlockEntity))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (player.isCrouching() && !level.isClientSide())
        {
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(stewPotBlockEntity, Component.literal("Stew Pot")), pos);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getCapability(Capabilities.FluidHandler.ITEM) == null) {
            boolean tryInsert = stewPotBlockEntity.tryInteract(pos, player, stack);

            if (!tryInsert && !level.isClientSide())
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(stewPotBlockEntity, Component.literal("Stew Pot")), pos);

            return ItemInteractionResult.SUCCESS;
        }

        // todo: allow water bottles to add/get water from the stew pot
        boolean interaction = FluidUtil.interactWithFluidHandler(player, hand, level, pos, null);

        if (!interaction && !level.isClientSide())
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(stewPotBlockEntity, Component.literal("Stew Pot")), pos);

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }
}