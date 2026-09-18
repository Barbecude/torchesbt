package com.enchantedwisp.torchesbt.core;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized registry for managing burnable items and blocks, their lit/unlit states, and burn properties.
 * <p>
 * This registry serves as the authoritative source for querying and registering burnables.
 * Both items and blocks can be registered with their burn duration, environmental multipliers,
 * and in the case of blocks, their fuel type and block entity support.
 * </p>
 * <p>
 * Registered entries are stored in-memory and can be queried at runtime to determine
 * whether an item or block is burnable, how long it should last, and how its burn time
 * is affected by rain or water.
 * </p>
 * <p>
 * <strong>Note:</strong> Re-registering the same lit item/block will overwrite the previous entry.
 * </p>
 */
public class BurnableRegistry {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    private static final Map<Item, BurnableItemEntry> BURNABLE_ITEMS = new HashMap<>();
    private static final Map<Block, BurnableBlockEntry> BURNABLE_BLOCKS = new HashMap<>();

    private static final Map<String, Integer> SOURCE_ITEM_COUNTS = new HashMap<>();
    private static final Map<String, Integer> SOURCE_BLOCK_COUNTS = new HashMap<>();

    /**
     * @return The total number of registered burnable items across all sources.
     */
    public static int getBurnableItemsCount() {
        return BURNABLE_ITEMS.size();
    }

    /**
     * @return The total number of registered burnable blocks across all sources.
     */
    public static int getBurnableBlocksCount() {
        return BURNABLE_BLOCKS.size();
    }

    /**
     * Takes a snapshot of the current burnable counts and saves them under the given source name.
     *
     * @param source The identifier of the source (e.g., "Vanilla", "Chipped").
     */
    public static void snapshotCounts(String source) {
        SOURCE_ITEM_COUNTS.put(source, getBurnableItemsCount());
        SOURCE_BLOCK_COUNTS.put(source, getBurnableBlocksCount());
    }

    /**
     * Logs the number of burnables registered for the given source.
     * If no snapshot exists for that source, defaults to 0 items/blocks.
     *
     * @param source The identifier of the source (e.g., "Vanilla", "Chipped").
     * @param logger The logger instance used to print the message.
     */
    public static void logSource(String source, Logger logger) {
        int items = SOURCE_ITEM_COUNTS.getOrDefault(source, 0);
        int blocks = SOURCE_BLOCK_COUNTS.getOrDefault(source, 0);
        logger.info("{} burnables registered: {} items, {} blocks", source, items, blocks);
    }

    /**
     * Registers a burnable item with its properties.
     *
     * @param litItem         the item in its lit state
     * @param unlitItem       the item in its unlit state
     * @param burnTime        duration the item burns for (in ticks)
     * @param rainMultiplier  multiplier applied when exposed to rain
     * @param waterMultiplier multiplier applied when submerged in water
     */
    public static void registerBurnableItem(Item litItem, Item unlitItem,
                                             long burnTime, double rainMultiplier, double waterMultiplier, boolean enableTicking) {
        BURNABLE_ITEMS.put(litItem, new BurnableItemEntry(litItem, unlitItem, burnTime, rainMultiplier, waterMultiplier, enableTicking));
        LOGGER.debug("Registered burnable item: {} (unlit: {}, ticking: {})", Registries.ITEM.getId(litItem), Registries.ITEM.getId(unlitItem), enableTicking);
    }

    /**
     * Registers a burnable block with its properties and fuel type.
     *
     * @param litBlock        the block in its lit state
     * @param unlitBlock      the block in its unlit state
     * @param burnTime        duration the block burns for (in ticks)
     * @param rainMultiplier  multiplier applied when exposed to rain
     * @param waterMultiplier multiplier applied when submerged in water
     * @param hasBlockEntity  whether the block has a block entity
     * @param fuelType        the type of fuel used by the block
     */
    public static void registerBurnableBlock(
            Block litBlock,
            Block unlitBlock,
            long burnTime,
            double rainMultiplier,
            double waterMultiplier,
            boolean hasBlockEntity,
            FuelTypeAPI.FuelType fuelType,
            boolean enableTicking
    ) {
        BURNABLE_BLOCKS.put(litBlock, new BurnableBlockEntry(
                litBlock, unlitBlock, burnTime, rainMultiplier, waterMultiplier, hasBlockEntity, fuelType, enableTicking
        ));
        LOGGER.debug("Registered burnable block: {} (unlit: {}, fuelType: {}, ticking: {})",
                Registries.BLOCK.getId(litBlock),
                Registries.BLOCK.getId(unlitBlock),
                fuelType != null ? fuelType.getId() : "null",
                enableTicking
        );
    }

    // --- Query Methods ---
    public static boolean isBurnableItem(Item item) { return BURNABLE_ITEMS.containsKey(item); }
    public static boolean isBurnableBlock(Block block) { return BURNABLE_BLOCKS.containsKey(block); }

    public static Item getUnlitItem(Item litItem) {
        BurnableItemEntry entry = BURNABLE_ITEMS.get(litItem);
        return entry != null ? entry.unlitItem() : null;
    }

    public static Item getLitItem(Item unlitItem) {
        return BURNABLE_ITEMS.values().stream()
                .filter(entry -> entry.unlitItem() == unlitItem)
                .map(BurnableItemEntry::litItem)
                .findFirst()
                .orElse(null);
    }

    public static Block getUnlitBlock(Block litBlock) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(litBlock);
        return entry != null ? entry.unlitBlock() : null;
    }

    public static Block getLitBlock(Block unlitBlock) {
        return BURNABLE_BLOCKS.values().stream()
                .filter(entry -> entry.unlitBlock() == unlitBlock)
                .map(BurnableBlockEntry::litBlock)
                .findFirst()
                .orElse(null);
    }

    public static long getBurnTime(Item item) {
        BurnableItemEntry entry = BURNABLE_ITEMS.get(item);
        return entry != null ? entry.burnTime() : 0;
    }

    public static long getBurnTime(Block block) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(block);
        return entry != null ? entry.burnTime() : 0;
    }

    public static double getRainMultiplier(Item item) {
        BurnableItemEntry entry = BURNABLE_ITEMS.get(item);
        return entry != null ? entry.rainMultiplier() : 1.0;
    }

    public static double getRainMultiplier(Block block) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(block);
        return entry != null ? entry.rainMultiplier() : 1.0;
    }

    public static double getWaterMultiplier(Item item) {
        BurnableItemEntry entry = BURNABLE_ITEMS.get(item);
        return entry != null ? entry.waterMultiplier() : 1.0;
    }

    public static double getWaterMultiplier(Block block) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(block);
        return entry != null ? entry.waterMultiplier() : 1.0;
    }

    public static boolean hasBlockEntity(Block block) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(block);
        return entry != null && entry.hasBlockEntity();
    }

    public static boolean isTickingEnabled(Item item) {
        BurnableItemEntry entry = BURNABLE_ITEMS.get(item);
        return entry != null && entry.enableTicking();
    }

    public static boolean isTickingEnabled(Block block) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(block);
        return entry != null && entry.enableTicking();
    }

    public static FuelTypeAPI.FuelType getFuelType(Block block) {
        BurnableBlockEntry entry = BURNABLE_BLOCKS.get(block);
        return entry != null ? entry.fuelType() : null;
    }

    /** Record representing a burnable item and its properties */
    public record BurnableItemEntry(Item litItem, Item unlitItem, long burnTime, double rainMultiplier, double waterMultiplier, boolean enableTicking) {}

    /** Record representing a burnable block and its properties */
    public record BurnableBlockEntry(Block litBlock, Block unlitBlock, long burnTime, double rainMultiplier, double waterMultiplier, boolean hasBlockEntity, FuelTypeAPI.FuelType fuelType, boolean enableTicking) {}
}
