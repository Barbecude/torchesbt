package com.enchantedwisp.torchesbt.compat.chipped.item;

import com.enchantedwisp.torchesbt.compat.chipped.block.SpecialUnlitLanternBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

public class ChippedSpecialUnlitLanternItem extends BlockItem {
    public ChippedSpecialUnlitLanternItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        boolean isHanging = !context.getWorld().getBlockState(context.getBlockPos().up()).isAir();
        BlockState state = getBlock().getDefaultState()
                .with(SpecialUnlitLanternBlock.FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(SpecialUnlitLanternBlock.HANGING, isHanging)
                .with(SpecialUnlitLanternBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        return state.canPlaceAt(context.getWorld(), context.getBlockPos()) ? state : null;
    }
}