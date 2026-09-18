package com.enchantedwisp.torchesbt.api;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.burn.Burnable;
import com.enchantedwisp.torchesbt.core.ignition.IgnitionHandler;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Public API for querying, modifying, and igniting burnables.
 * <p>
 * Supports both:
 * <ul>
 *   <li><b>ItemStacks</b> — e.g., torches, lanterns, or custom items.</li>
 *   <li><b>Blocks</b> — e.g., placed lanterns, campfires, or custom burnable blocks.</li>
 * </ul>
 * Burn time values are expressed in ticks.
 */
public class BurnTime {

    /**
     * Gets the remaining burn time for a burnable {@link ItemStack}.
     *
     * @param stack the item stack to query
     * @return the remaining burn time in ticks, or {@code 0} if not burnable
     */
    public static long getBurnTime(ItemStack stack) {
        if (!BurnableRegistry.isBurnableItem(stack.getItem())) {
            return 0;
        }
        return BurnTimeUtils.getCurrentBurnTime(stack);
    }

    /**
     * Sets the remaining burn time for a burnable {@link ItemStack}.
     * <p>
     * The value is clamped between {@code 0} and the maximum burn time
     * defined for the item.
     *
     * @param stack    the item stack to modify
     * @param burnTime the new burn time in ticks
     */
    public static void setBurnTime(ItemStack stack, long burnTime) {
        if (!BurnableRegistry.isBurnableItem(stack.getItem())) {
            return;
        }
        BurnTimeUtils.setCurrentBurnTime(stack, burnTime);
    }

    /**
     * Gets the remaining burn time for a burnable block at the given position.
     *
     * @param world the world containing the block
     * @param pos   the position of the block
     * @return the remaining burn time in ticks, or {@code 0} if not burnable
     */
    public static long getBurnTime(World world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity == null || !BurnableRegistry.isBurnableBlock(world.getBlockState(pos).getBlock())) {
            return 0;
        }
        if (entity instanceof ICampfireBurnAccessor accessor) {
            return accessor.torchesbt_getBurnTime();
        }
        return BurnTimeUtils.getCurrentBurnTime(entity);
    }

    /**
     * Sets the remaining burn time for a burnable block at the given position.
     * <p>
     * The value is clamped between {@code 0} and the maximum burn time
     * defined for the block. If set to {@code 0}, the block may extinguish
     * and trigger a state update.
     *
     * @param world    the world containing the block
     * @param pos      the position of the block
     * @param burnTime the new burn time in ticks
     */
    public static void setBurnTime(World world, BlockPos pos, long burnTime) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity == null || !BurnableRegistry.isBurnableBlock(world.getBlockState(pos).getBlock())) {
            return;
        }
        long clamped = Math.max(0, Math.min(burnTime, BurnableRegistry.getBurnTime(world.getBlockState(pos).getBlock())));
        if (entity instanceof ICampfireBurnAccessor accessor) {
            accessor.torchesbt_setBurnTime(clamped);
        } else {
            // Assume custom blocks implement Burnable
            ((Burnable) entity).setRemainingBurnTime(clamped);
        }
        if (clamped <= 0) {
            // Let the core mod handle replacement during its tick
            world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    /**
     * Converts an unlit burnable {@link ItemStack} to its lit version.
     *
     * @param stack    the unlit item stack
     * @param burnTime the initial burn time for the lit stack
     * @return the lit item stack, or the original if not ignitable
     */
    public static ItemStack igniteItem(ItemStack stack, long burnTime) {
        Item litItem = BurnableRegistry.getLitItem(stack.getItem());
        if (litItem == null) {
            return stack;
        }
        ItemStack litStack = new ItemStack(litItem, stack.getCount());
        setBurnTime(litStack, burnTime);
        return litStack;
    }

    /**
     * Converts an unlit block to its lit version at the specified position.
     * <p>
     * Preserves block state properties where possible and applies the given burn time.
     *
     * @param world    the world
     * @param pos      the block position
     * @param burnTime the initial burn time for the lit block
     * @return {@code true} if the block was ignited, {@code false} otherwise
     */
    public static boolean igniteBlock(World world, BlockPos pos, long burnTime) {
        BlockState state = world.getBlockState(pos);
        Block litBlock = BurnableRegistry.getLitBlock(state.getBlock());
        if (litBlock == null || litBlock == state.getBlock()) {
            return false;
        }
        BlockState newState = IgnitionHandler.copyProperties(state, litBlock.getDefaultState());
        world.setBlockState(pos, newState, 3);
        setBurnTime(world, pos, burnTime);
        return true;
    }

    /**
     * Hooks that run during `processPlayerItems`.
     * Allows compat mods to add extra tick behavior for players.
     */
    private static final List<Consumer<PlayerEntity>> PLAYER_ITEM_TICK_HANDLERS = new ArrayList<>();

    /**
     * Registers a callback to be invoked during `processPlayerItems`.
     *
     * @param handler The callback, receiving the player as an argument
     */
    public static void registerPlayerItemTickHandler(Consumer<PlayerEntity> handler) {
        PLAYER_ITEM_TICK_HANDLERS.add(handler);
    }

    /**
     * Called internally by BurnTimeManager when ticking player items.
     */
    public static void runPlayerItemTickHandlers(PlayerEntity player) {
        for (Consumer<PlayerEntity> handler : PLAYER_ITEM_TICK_HANDLERS) {
            try {
                handler.accept(player);
            } catch (Exception e) {
                // Avoid crashing the tick if a compat mod fails
                RealisticTorchesBT.LOGGER.warn("Error in player item tick handler", e);
            }
        }
    }
}
