package com.enchantedwisp.torchesbt.core.fuel;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.burn.Burnable;
import com.enchantedwisp.torchesbt.core.ignition.ReignitionHandler;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

/**
 * Handles fueling of burnable blocks using FuelTypeAPI fuel types.
 */
public class FuelHandler {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    /**
     * Registers fueling logic for burnable blocks.
     * Triggered when a player right-clicks with a fuel item.
     */
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            BlockEntity entity = world.getBlockEntity(pos);
            Block block = world.getBlockState(pos).getBlock();

            // Fueling for burnable blocks
            if (BurnableRegistry.isBurnableBlock(block)) {
                boolean isCampfire = block == Blocks.CAMPFIRE;
                boolean isLit = isCampfire ? world.getBlockState(pos).get(CampfireBlock.LIT) : true;

                FuelTypeAPI.FuelType fuelType = BurnableRegistry.getFuelType(block);
                if (isLit && fuelType != null && fuelType.getFuelMap().containsKey(itemId)) {
                    long current;
                    long added = FuelTypeAPI.getFuelBurnTime(fuelType, itemId);

                    if (entity instanceof Burnable burnable) {
                        current = burnable.getRemainingBurnTime();
                        burnable.setRemainingBurnTime(current + added);
                    } else if (entity instanceof ICampfireBurnAccessor accessor) {
                        current = accessor.torchesbt_getBurnTime();
                        accessor.torchesbt_setBurnTime(current + added);
                        entity.markDirty();
                    } else {
                        return ActionResult.PASS;
                    }

                    world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, 1);
                    ReignitionHandler.consumeFuel(stack, player, hand);
                    LOGGER.debug("Fueled block {} at {} by {} using {}, {} -> {}",
                            block, pos, player.getName().getString(), stack.getItem(), current, current + added);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }
}