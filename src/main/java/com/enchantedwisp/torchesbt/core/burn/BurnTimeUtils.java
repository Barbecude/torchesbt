package com.enchantedwisp.torchesbt.core.burn;

import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.util.ConfigCache;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Objects;

/**
 * Utility methods for handling burn times, rain checks.
 */
public class BurnTimeUtils {
    public static final String BURN_TIME_KEY = "remaining_burn";

    public static long getMaxBurnTime(ItemStack stack) {
        return BurnableRegistry.getBurnTime(stack.getItem());
    }

    public static long getCurrentBurnTime(ItemStack stack) {
        return stack.hasNbt() && Objects.requireNonNull(stack.getNbt()).contains(BURN_TIME_KEY)
                ? stack.getNbt().getLong(BURN_TIME_KEY)
                : getMaxBurnTime(stack);
    }

    public static void setCurrentBurnTime(ItemStack stack, long burnTime) {
        stack.getOrCreateNbt().putLong(BURN_TIME_KEY, Math.min(burnTime, getMaxBurnTime(stack)));
    }

    public static long getCurrentBurnTime(BlockEntity entity) {
        if (entity instanceof Burnable burnable) {
            return burnable.getRemainingBurnTime();
        }
        if (entity instanceof ICampfireBurnAccessor accessor) {
            return accessor.torchesbt_getBurnTime();
        }
        return 0;
    }

    // --- Centralized rain check ---
    public static boolean isActuallyRainingAt(World world, BlockPos pos) {
        if (world.getFluidState(pos).isIn(FluidTags.WATER)) return false; // Submersion handled separately
        Biome biome = world.getBiome(pos).value();
        Biome.Precipitation precipitation = biome.getPrecipitation(pos);
        return ConfigCache.isRainExtinguishEnabled() &&
                world.isRaining() &&
                world.isSkyVisible(pos) &&
                (precipitation == Biome.Precipitation.RAIN || precipitation == Biome.Precipitation.SNOW);
    }
}