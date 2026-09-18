package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.core.FlameLevel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements ICampfireBurnAccessor {
    @Unique
    private long torchesbt_burnTime = 0;

    public CampfireBlockEntityMixin(BlockPos pos, BlockState state) {
        super(BlockEntityType.CAMPFIRE, pos, state);
    }

    @Override
    public long torchesbt_getBurnTime() {
        return torchesbt_burnTime;
    }

    @Override
    public void torchesbt_setBurnTime(long time) {
        long max = BurnableRegistry.getBurnTime(getCachedState().getBlock());
        this.torchesbt_burnTime = Math.max(0, Math.min(time, max));
        markDirty();

        if (world != null && !world.isClient) {
            BlockState state = getCachedState();

            // Decide flame level
            FlameLevel level = FlameLevel.LOW;
            double pct = (double) this.torchesbt_burnTime / max;
            if (pct > 0.50) {
                level = FlameLevel.FULL;
            } else if (pct > 0.20) {
                level = FlameLevel.MID;
            }

            world.setBlockState(
                    pos,
                    state.with(CampfireBlock.LIT, this.torchesbt_burnTime > 0)
                            .with(FlameLevel.PROPERTY, level),
                    Block.NOTIFY_ALL
            );
        }
    }


    @Override
    public DefaultedList<ItemStack> torchesbt_getItems() {
        return ((CampfireBlockEntity) (Object) this).getItemsBeingCooked();
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void torchesbt_writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putLong(BurnTimeUtils.BURN_TIME_KEY, torchesbt_burnTime);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void torchesbt_readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(BurnTimeUtils.BURN_TIME_KEY)) {
            torchesbt_burnTime = nbt.getLong(BurnTimeUtils.BURN_TIME_KEY);
            if (world != null && !world.isClient) {
                world.setBlockState(pos, getCachedState().with(CampfireBlock.LIT, torchesbt_burnTime > 0), 3);
                RealisticTorchesBT.LOGGER.debug("Loaded campfire at {} with burn time {}, LIT={}", pos, torchesbt_burnTime, torchesbt_burnTime > 0);
            }
        }
    }

    @Inject(method = "toUpdatePacket*", at = @At("RETURN"), cancellable = true)
    private void toUpdatePacket(CallbackInfoReturnable<BlockEntityUpdateS2CPacket> cir) {
        NbtCompound nbt = createNbt();
        nbt.putLong(BurnTimeUtils.BURN_TIME_KEY, torchesbt_burnTime);
        cir.setReturnValue(BlockEntityUpdateS2CPacket.create(this, blockEntity -> nbt));
    }

    @Inject(method = "toInitialChunkDataNbt", at = @At("RETURN"), cancellable = true)
    private void toInitialChunkDataNbt(CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound nbt = cir.getReturnValue();
        nbt.putLong(BurnTimeUtils.BURN_TIME_KEY, torchesbt_burnTime);
        cir.setReturnValue(nbt);
    }
}