package com.enchantedwisp.torchesbt.blockentity;

import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.burn.Burnable;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Block entity for lanterns, tracking their burn time.
 * Implements Burnable for standardized burn time management.
 */
public class LanternBlockEntity extends BlockEntity implements Burnable {
    private static final String REMAINING_KEY = "remaining_burn";
    private long remainingBurnTime;

    public LanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LANTERN_BLOCK_ENTITY, pos, state);
        this.remainingBurnTime = getMaxBurnTime();
    }

    @Override
    public long getMaxBurnTime() {
        return BurnableRegistry.getBurnTime(getCachedState().getBlock());
    }

    @Override
    public long getRemainingBurnTime() {
        return remainingBurnTime;
    }

    @Override
    public void setRemainingBurnTime(long time) {
        this.remainingBurnTime = Math.max(0, Math.min(time, getMaxBurnTime()));
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public void tickBurn(World world, boolean isBlock) {
        if (remainingBurnTime <= 0) return;

        boolean isRaining = isBlock && BurnTimeUtils.isActuallyRainingAt(world, pos);
        boolean isSubmerged = world.getFluidState(pos).isIn(FluidTags.WATER);
        double rainMult = getRainMultiplier();
        double waterMult = getWaterMultiplier();

        if (isSubmerged && waterMult == 10.0) {
            setRemainingBurnTime(0);
            return;
        }

        double effectiveMultiplier = 1.0;
        if (isRaining) {
            effectiveMultiplier = Math.max(effectiveMultiplier, rainMult);
        }
        if (isSubmerged && waterMult > 0.0) {
            effectiveMultiplier = Math.max(effectiveMultiplier, waterMult);
        }
        long reduction = (long) Math.ceil(effectiveMultiplier);
        setRemainingBurnTime(remainingBurnTime - reduction);
    }

    @Override
    public double getRainMultiplier() {
        return BurnableRegistry.getRainMultiplier(getCachedState().getBlock());
    }

    @Override
    public double getWaterMultiplier() {
        return BurnableRegistry.getWaterMultiplier(getCachedState().getBlock());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        remainingBurnTime = nbt.getLong(REMAINING_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong(REMAINING_KEY, remainingBurnTime);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}