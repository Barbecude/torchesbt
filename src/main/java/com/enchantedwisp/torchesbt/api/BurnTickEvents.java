package com.enchantedwisp.torchesbt.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Events fired during burn time ticking.
 * <p>
 * These events allow mods to intercept and modify the burn time decrement
 * for burnables in three contexts:
 * <ul>
 *   <li>{@link #PLAYER_HELD} — when the item is held by a player</li>
 *   <li>{@link #DROPPED_ITEM} — when the item exists as a dropped {@link ItemEntity}</li>
 *   <li>{@link #BLOCK} — when the item is placed as a block in the world</li>
 * </ul>
 * <p>
 * Each listener receives the current decrement value and may return a modified one.
 * All registered listeners are called sequentially, with each one receiving the
 * result of the previous. Returning {@code 0} cancels burn reduction for that tick.
 * </p>
 * <p>
 * <strong>Note:</strong> Returning negative values is not recommended unless explicitly
 * supported by the caller. By default, negative decrements are treated as {@code 0}.
 * </p>
 */
public class BurnTickEvents {

    /**
     * Event fired when a burnable is held by a player and its burn time is ticked.
     */
    public static final Event<PlayerHeld> PLAYER_HELD = EventFactory.createArrayBacked(
            PlayerHeld.class,
            listeners -> (context, currentDecrement) -> {
                long modified = currentDecrement;
                for (PlayerHeld listener : listeners) {
                    modified = listener.onTick(context, modified);
                }
                return modified;
            }
    );

    /**
     * Event fired when a dropped burnable item entity has its burn time ticked.
     */
    public static final Event<DroppedItem> DROPPED_ITEM = EventFactory.createArrayBacked(
            DroppedItem.class,
            listeners -> (context, currentDecrement) -> {
                long modified = currentDecrement;
                for (DroppedItem listener : listeners) {
                    modified = listener.onTick(context, modified);
                }
                return modified;
            }
    );

    /**
     * Event fired when a placed burnable block has its burn time ticked.
     */
    public static final Event<Block> BLOCK = EventFactory.createArrayBacked(
            Block.class,
            listeners -> (context, currentDecrement) -> {
                long modified = currentDecrement;
                for (Block listener : listeners) {
                    modified = listener.onTick(context, modified);
                }
                return modified;
            }
    );


    /**
     * Listener interface for the {@link #PLAYER_HELD} event.
     * <p>
     * Called when a burnable held in a player's inventory is ticked for burn time.
     * Implementations may adjust the decrement value dynamically — for example,
     * to slow down burning if the player has fire resistance.
     * </p>
     */
    public interface PlayerHeld {
        /**
         * Called when a held burnable's burn time is ticked.
         *
         * @param context          event context with player, stack, and base decrement
         * @param currentDecrement the decrement value passed from the previous listener
         * @return the new decrement to apply (0 prevents decrementing further)
         */
        long onTick(PlayerHeldContext context, long currentDecrement);
    }

    /**
     * Listener interface for the {@link #DROPPED_ITEM} event.
     * <p>
     * Called when a burnable item entity that exists in the world is ticked.
     * Implementations may adjust the decrement value dynamically.
     * </p>
     */
    public interface DroppedItem {
        /**
         * Called when a dropped burnable item's burn time is ticked.
         *
         * @param context          event context with entity, stack, and base decrement
         * @param currentDecrement the decrement value passed from the previous listener
         * @return the new decrement to apply (0 prevents decrementing further)
         */
        long onTick(DroppedItemContext context, long currentDecrement);
    }

    /**
     * Listener interface for the {@link #BLOCK} event.
     * <p>
     * Called when a burnable block placed in the world is ticked.
     * Implementations may adjust the decrement value dynamically.
     * </p>
     */
    public interface Block {
        /**
         * Called when a placed burnable block's burn time is ticked.
         *
         * @param context          event context with world, position, and base decrement
         * @param currentDecrement the decrement value passed from the previous listener
         * @return the new decrement to apply (0 prevents decrementing further)
         */
        long onTick(BlockContext context, long currentDecrement);
    }


    /**
     * Context for the {@link #PLAYER_HELD} event.
     *
     * @param player    the player holding the burnable
     * @param stack     the item stack representing the burnable
     * @param decrement the base decrement value for this tick (before modification)
     */
    public record PlayerHeldContext(
            PlayerEntity player,
            ItemStack stack,
            long decrement
    ) {
    }

    /**
     * Context for the {@link #DROPPED_ITEM} event.
     *
     * @param entity    the dropped item entity
     * @param stack     the item stack contained in the entity
     * @param decrement the base decrement value for this tick (before modification)
     */
    public record DroppedItemContext(
            ItemEntity entity,
            ItemStack stack,
            long decrement
    ) {
    }

    /**
     * Context for the {@link #BLOCK} event.
     *
     * @param world     the world containing the burnable block
     * @param pos       the block position of the burnable
     * @param decrement the base decrement value for this tick (before modification)
     */
    public record BlockContext(
            World world,
            BlockPos pos,
            long decrement
    ) {
    }
}
