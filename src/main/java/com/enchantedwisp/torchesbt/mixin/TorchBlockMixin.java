package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.blockentity.TorchBlockEntity;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeManager;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to add block entity support and burn time handling for TorchBlock.
 */
@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin extends Block implements BlockEntityProvider {
    public TorchBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TorchBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof TorchBlockEntity) {
            BurnTimeManager.setBurnTimeOnPlacement(world, pos, entity, itemStack, BurnableRegistry.getBurnTime(state.getBlock()));
        }
    }
}