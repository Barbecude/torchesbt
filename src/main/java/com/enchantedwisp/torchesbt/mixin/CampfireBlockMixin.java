package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.core.FlameLevel;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends Block {

    public CampfireBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void appendFlameLevel(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(FlameLevel.PROPERTY);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && state.contains(CampfireBlock.LIT) && state.get(CampfireBlock.LIT)) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof ICampfireBurnAccessor campfire) {
                long burnTime = BurnTimeUtils.getCurrentBurnTime(itemStack);
                if (burnTime <= 0) {
                    burnTime = BurnableRegistry.getBurnTime(state.getBlock());
                }
                campfire.torchesbt_setBurnTime(burnTime);
            }
        }
    }
}

