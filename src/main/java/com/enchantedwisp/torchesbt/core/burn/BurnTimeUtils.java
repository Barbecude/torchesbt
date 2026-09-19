package com.enchantedwisp.torchesbt.core.burn;

import com.enchantedwisp.torchesbt.registry.ModDataComponentTypes;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.util.ConfigCache;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Utility methods for handling burn times, rain checks.
 */
public class BurnTimeUtils {
    public static final String BURN_TIME_KEY = "remaining_burn";

    public static long getMaxBurnTime(ItemStack stack) {
        if (stack.isOf(net.minecraft.item.Items.TORCH)) {
            return Math.max(3600L, BurnableRegistry.getBurnTime(stack.getItem()));
        }
        return BurnableRegistry.getBurnTime(stack.getItem());
    }

    public static long getCurrentBurnTime(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.REMAINING_BURN, getMaxBurnTime(stack));
    }

    public static void setCurrentBurnTime(ItemStack stack, long burnTime) {
        stack.set(ModDataComponentTypes.REMAINING_BURN, Math.min(burnTime, getMaxBurnTime(stack)));
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
        if (!world.isRaining()) return false;
        if (!world.isSkyVisible(pos)) return false;
        if (world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return false;
        }
        Biome biome = world.getBiome(pos).value();
        Biome.Precipitation precipitation = biome.getPrecipitation(pos);
        return ConfigCache.isRainExtinguishEnabled() &&
                (precipitation == Biome.Precipitation.RAIN || precipitation == Biome.Precipitation.SNOW);
    }
}