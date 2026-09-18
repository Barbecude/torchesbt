package com.enchantedwisp.torchesbt.core.ignition;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.fuel.FuelHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.slf4j.Logger;

/**
 * Entry point for ignition and fueling systems.
 *
 * <p>Delegates ignition to {@link IgnitionHandler}
 * and fueling to {@link FuelHandler}. Also provides
 * shared helper methods for consuming items.</p>
 */
public class ReignitionHandler {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    /**
     * Registers both ignition and fueling logic.
     */
    public static void register() {
        IgnitionHandler.register();
        FuelHandler.register();
    }

    // -------------------------------
    // Shared helpers
    // -------------------------------

    /**
     * Consumes or damages an igniter (e.g. Flint & Steel).
     *
     * @param stack  The igniter stack
     * @param player The player using the igniter
     * @param hand   The hand holding it
     */
    protected static void consumeIgniter(ItemStack stack, PlayerEntity player, Hand hand) {
        if (stack.isDamageable()) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
        } else if (!player.isCreative()) {
            stack.decrement(1);
        }
    }

    /**
     * Consumes a fuel item (decreases stack size).
     *
     * @param stack  The fuel stack
     * @param player The player using the fuel
     * @param hand   The hand holding it
     */
    public static void consumeFuel(ItemStack stack, PlayerEntity player, Hand hand) {
        if (!player.isCreative()) {
            stack.decrement(1);
        }
    }
}
