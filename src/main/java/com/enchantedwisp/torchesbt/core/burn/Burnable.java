package com.enchantedwisp.torchesbt.core.burn;

import net.minecraft.world.World;

/**
 * Interface for entities (blocks or items) that have a burn time, such as torches, lanterns, and campfires.
 * Provides standardized methods for managing burn time and rain effects.
 */
public interface Burnable {
    /**
     * Gets the maximum burn time in ticks.
     *
     * @return The maximum burn time.
     */
    long getMaxBurnTime();

    /**
     * Gets the current remaining burn time in ticks.
     *
     * @return The current burn time.
     */
    long getRemainingBurnTime();

    /**
     * Sets the current remaining burn time, clamped between 0 and max burn time.
     *
     * @param time The burn time to set.
     */
    void setRemainingBurnTime(long time);

    /**
     * Reduces the burn time by one tick, applying rain multipliers if applicable.
     *
     * @param world   The world the burnable is in.
     * @param isBlock Whether the burnable is a block (affects rain checks).
     */
    void tickBurn(World world, boolean isBlock);

    /**
     * Gets the rain multiplier for burn time reduction.
     *
     * @return The multiplier (e.g., 2.0 for 2x faster burnout in rain).
     */
    double getRainMultiplier();

    double getWaterMultiplier();
}