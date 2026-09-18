package com.enchantedwisp.torchesbt.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Events fired during ignition attempts for burnable items or blocks.
 * <p>
 * These events allow mods to intercept, cancel, or modify ignition logic:
 * <ul>
 *   <li><b>Item ignition:</b> When a burnable item (in hand or offhand) is ignited.</li>
 *   <li><b>Block ignition:</b> When a placed burnable block is ignited.</li>
 * </ul>
 * Listeners may:
 * <ul>
 *   <li>Return {@link ActionResult#FAIL} to cancel the ignition.</li>
 *   <li>Return {@link ActionResult#SUCCESS} to force ignition.</li>
 *   <li>Return {@link ActionResult#PASS} to defer to the next listener.</li>
 * </ul>
 * The proposed burn time may also be modified through the event context.
 */
public class IgnitionEvents {

    /**
     * Fired before a burnable item (in hand or offhand) is ignited.
     * <p>
     * Listeners can cancel, force, or allow the ignition, and may modify
     * {@link ItemContext#proposedBurnTime} to change the burn duration.
     */
    public static final Event<Item> ITEM = EventFactory.createArrayBacked(Item.class, listeners -> context -> {
        for (Item listener : listeners) {
            ActionResult result = listener.onIgnite(context);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });

    /**
     * Fired before a placed burnable block is ignited.
     * <p>
     * Listeners can cancel, force, or allow the ignition, and may modify
     * {@link BlockContext#proposedBurnTime} to change the burn duration.
     */
    public static final Event<Block> BLOCK = EventFactory.createArrayBacked(Block.class, listeners -> context -> {
        for (Block listener : listeners) {
            ActionResult result = listener.onIgnite(context);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });

    /**
     * Listener interface for {@link #ITEM}.
     * <p>
     * Register an implementation to receive callbacks before
     * a burnable item is ignited.
     */
    public interface Item {
        /**
         * Called before igniting a burnable item.
         *
         * @param context the event context, including player, hands, stacks, and proposed burn time
         * @return {@link ActionResult#FAIL} to cancel, {@link ActionResult#SUCCESS} to force ignition,
         * or {@link ActionResult#PASS} to continue
         */
        ActionResult onIgnite(ItemContext context);
    }

    /**
     * Listener interface for {@link #BLOCK}.
     * <p>
     * Register an implementation to receive callbacks before
     * a burnable block is ignited.
     */
    public interface Block {
        /**
         * Called before igniting a burnable block.
         *
         * @param context the event context, including player, world, position, stack, and proposed burn time
         * @return {@link ActionResult#FAIL} to cancel, {@link ActionResult#SUCCESS} to force ignition,
         * or {@link ActionResult#PASS} to continue
         */
        ActionResult onIgnite(BlockContext context);
    }

    /**
     * Context object for {@link #ITEM} events.
     * <p>
     * Provides access to the player, relevant hands, item stacks, and the
     * current proposed burn time. Burn time can be adjusted with
     * {@link #setProposedBurnTime(long)}.
     */
    public static class ItemContext {
        private final PlayerEntity player;
        private final Hand burnableHand;
        private final ItemStack burnableStack;
        private final ItemStack igniterStack;
        private long proposedBurnTime;

        /**
         * Creates a new item ignition context.
         *
         * @param player           the player attempting ignition
         * @param burnableHand     the hand holding the burnable item
         * @param burnableStack    the burnable item stack
         * @param igniterStack     the item stack used to ignite (e.g., flint and steel)
         * @param proposedBurnTime the initial proposed burn time
         */
        public ItemContext(PlayerEntity player, Hand burnableHand, ItemStack burnableStack, ItemStack igniterStack, long proposedBurnTime) {
            this.player = player;
            this.burnableHand = burnableHand;
            this.burnableStack = burnableStack;
            this.igniterStack = igniterStack;
            this.proposedBurnTime = proposedBurnTime;
        }

        public PlayerEntity getPlayer() {
            return player;
        }

        public Hand getBurnableHand() {
            return burnableHand;
        }

        public ItemStack getBurnableStack() {
            return burnableStack;
        }

        public ItemStack getIgniterStack() {
            return igniterStack;
        }

        public long getProposedBurnTime() {
            return proposedBurnTime;
        }

        public void setProposedBurnTime(long burnTime) {
            this.proposedBurnTime = burnTime;
        }
    }

    /**
     * Context object for {@link #BLOCK} events.
     * <p>
     * Provides access to the player, world, position, igniter stack, and
     * the current proposed burn time. Burn time can be adjusted with
     * {@link #setProposedBurnTime(long)}.
     */
    public static class BlockContext {
        private final PlayerEntity player;
        private final Hand igniterHand;
        private final World world;
        private final BlockPos pos;
        private final ItemStack igniterStack;
        private long proposedBurnTime;

        /**
         * Creates a new block ignition context.
         *
         * @param player           the player attempting ignition
         * @param igniterHand      the hand holding the igniter
         * @param world            the world containing the burnable block
         * @param pos              the position of the burnable block
         * @param igniterStack     the item stack used to ignite (e.g., flint and steel)
         * @param proposedBurnTime the initial proposed burn time
         */
        public BlockContext(PlayerEntity player, Hand igniterHand, World world, BlockPos pos, ItemStack igniterStack, long proposedBurnTime) {
            this.player = player;
            this.igniterHand = igniterHand;
            this.world = world;
            this.pos = pos;
            this.igniterStack = igniterStack;
            this.proposedBurnTime = proposedBurnTime;
        }

        public PlayerEntity getPlayer() {
            return player;
        }

        public Hand getIgniterHand() {
            return igniterHand;
        }

        public World getWorld() {
            return world;
        }

        public BlockPos getPos() {
            return pos;
        }

        public ItemStack getIgniterStack() {
            return igniterStack;
        }

        public long getProposedBurnTime() {
            return proposedBurnTime;
        }

        public void setProposedBurnTime(long burnTime) {
            this.proposedBurnTime = burnTime;
        }
    }
}