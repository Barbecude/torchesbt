package com.enchantedwisp.torchesbt.compat.chipped.item;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.compat.chipped.block.ChippedUnlitTorchBlock;
import com.enchantedwisp.torchesbt.compat.chipped.block.ChippedUnlitWallTorchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ChippedUnlitTorchItem extends BlockItem {
    private final String variant;

    public ChippedUnlitTorchItem(ChippedUnlitTorchBlock block, Settings settings, String variant) {
        super(block, settings);
        this.variant = variant;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getSide().getAxis().isHorizontal()) {
            BlockState wallState = Registries.BLOCK.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_wall_torch"))
                    .getDefaultState()
                    .with(ChippedUnlitWallTorchBlock.FACING, context.getSide());
            if (wallState.canPlaceAt(context.getWorld(), context.getBlockPos())) {
                return wallState;
            }
        }

        BlockState floorState = Registries.BLOCK.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_torch"))
                .getDefaultState();
        if (floorState.canPlaceAt(context.getWorld(), context.getBlockPos())) {
            return floorState;
        }

        return null;
    }
}