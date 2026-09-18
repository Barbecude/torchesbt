package com.enchantedwisp.torchesbt.registry.items;

import com.enchantedwisp.torchesbt.registry.RegistryHandler;
import com.enchantedwisp.torchesbt.registry.blocks.UnlitTorchBlock;
import com.enchantedwisp.torchesbt.registry.blocks.UnlitWallTorchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

public class UnlitTorchItem extends BlockItem {

    public UnlitTorchItem(UnlitTorchBlock block, Settings settings) {
        super(block, settings);
    }

    // Handle placement for both ground and wall torch variants
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        // Try placing wall torch if the clicked face is horizontal
        if (context.getSide().getAxis().isHorizontal()) {
            BlockState wallState = RegistryHandler.UNLIT_WALL_TORCH_BLOCK.getDefaultState()
                    .with(UnlitWallTorchBlock.FACING, context.getSide());
            if (wallState.canPlaceAt(context.getWorld(), context.getBlockPos())) {
                return wallState;
            }
        }

        // Fall back to ground torch
        BlockState floorState = RegistryHandler.UNLIT_TORCH_BLOCK.getDefaultState();
        if (floorState.canPlaceAt(context.getWorld(), context.getBlockPos())) {
            return floorState;
        }

        return null;
    }
}
