package com.enchantedwisp.torchesbt.registry.items;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnlitCampFireItem extends Item {
    public UnlitCampFireItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();

        ItemPlacementContext placementContext = new ItemPlacementContext(context);

        // Get the default placement state for a vanilla campfire
        BlockState state = Blocks.CAMPFIRE.getPlacementState(placementContext);
        if (state == null) {
            return ActionResult.FAIL;
        }
        // Force it to be unlit
        state = state
                .with(CampfireBlock.LIT, false)
                .with(CampfireBlock.SIGNAL_FIRE, false);

        BlockPos placePos = placementContext.getBlockPos();

        // Try placing
        if (!world.canSetBlock(placePos)) {
            return ActionResult.FAIL;
        }

        if (!world.isClient) {
            world.setBlockState(placePos, state, 3);

            assert player != null;
            player.playSound(SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1, 1);

            if (!player.isCreative()) {
                context.getStack().decrement(1);
            }
        }

        return ActionResult.success(world.isClient);
    }
}
