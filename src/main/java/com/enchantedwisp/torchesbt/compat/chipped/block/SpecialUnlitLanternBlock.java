package com.enchantedwisp.torchesbt.compat.chipped.block;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SpecialUnlitLanternBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty HANGING = Properties.HANGING;
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final VoxelShape DEFAULT_EAST_SHAPE = Block.createCuboidShape(5.0D, 0.0D, 1.0D, 11.0D, 15.0D, 15.0D);
    public static final VoxelShape DEFAULT_NORTH_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 5.0D, 15.0D, 15.0D, 11.0D);
    private final VoxelShape toEast;
    private final VoxelShape toNorth;

    public SpecialUnlitLanternBlock(Settings settings, VoxelShape shape) {
        super(settings.luminance(state -> 0)); // no light
        this.toEast = shape;
        this.toNorth = shape;
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(HANGING, false)
                .with(WATERLOGGED, false));
    }

    public SpecialUnlitLanternBlock(Settings settings, VoxelShape east, VoxelShape north) {
        super(settings.luminance(state -> 0));
        this.toEast = east;
        this.toNorth = north;
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(HANGING, false)
                .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HANGING, WATERLOGGED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(FACING);
        if (dir == Direction.EAST || dir == Direction.WEST) {
            return toEast;
        }
        return toNorth;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        FluidState fluid = ctx.getWorld().getFluidState(pos);

        boolean hanging = ctx.getSide() == Direction.DOWN
                || ctx.getWorld().getBlockState(pos.up()).isSideSolidFullSquare(ctx.getWorld(), pos.up(), Direction.DOWN);

        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(HANGING, hanging)
                .with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }
}
