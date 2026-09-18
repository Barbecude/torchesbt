package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.core.burn.BurnTimeManager;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to add block entity support and burn time handling for LanternBlock.
 */
@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin extends Block implements BlockEntityProvider {
    public LanternBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new com.enchantedwisp.torchesbt.blockentity.LanternBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof com.enchantedwisp.torchesbt.core.burn.Burnable) {
            BurnTimeManager.setBurnTimeOnPlacement(world, pos, entity, itemStack, BurnableRegistry.getBurnTime(state.getBlock()));
        }
    }
}