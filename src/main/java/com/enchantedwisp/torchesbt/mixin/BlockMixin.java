package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to modify block drop behavior for burnable blocks.
 */
@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "afterBreak",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"),
            cancellable = true)
    private void modifyDropsOnBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
        if (world.isClient) return;

        Block block = state.getBlock();
        if (BurnableRegistry.isBurnableBlock(block) && BurnableRegistry.hasBlockEntity(block)) {
            if (block instanceof CampfireBlock) {
                return;
            }

            if (blockEntity == null) return;

            long remainingBurnTime = BurnTimeUtils.getCurrentBurnTime(blockEntity);
            RealisticTorchesBT.LOGGER.debug("Block broken at {} with remaining burn time: {}", pos, remainingBurnTime);

            // Custom drop logic
            ItemStack drop;
            if (remainingBurnTime > 0) {
                // Drop lit item with remaining burn time
                drop = new ItemStack(block.asItem());
                BurnTimeUtils.setCurrentBurnTime(drop, remainingBurnTime);
            } else {
                // Drop unlit variant
                Item unlitItem = BurnableRegistry.getUnlitItem(block.asItem());
                drop = new ItemStack(unlitItem != null ? unlitItem : block.asItem());
            }
            Block.dropStack(world, pos, drop);
            RealisticTorchesBT.LOGGER.debug("Dropped item {} at {} with burn time {}", drop.getItem(), pos, remainingBurnTime);
            ci.cancel();
        }
    }
}