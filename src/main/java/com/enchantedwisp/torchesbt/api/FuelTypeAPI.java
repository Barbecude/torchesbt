    package com.enchantedwisp.torchesbt.api;

    import com.enchantedwisp.torchesbt.core.DefaultFuelTypes;
    import net.minecraft.item.Item;
    import net.minecraft.registry.tag.TagKey;
    import net.minecraft.util.Identifier;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import java.util.HashMap;
    import java.util.Map;

    /**
     * API for managing custom fuel types for burnable blocks or items.
     * Allows registering new fuel types and associating items or tags with burn times.
     *
     * <p>For examples of built-in fuel registration, see {@link DefaultFuelTypes}.
     */
    public class FuelTypeAPI {
        public static final String MOD_ID = "torchesbt";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
        private static final Map<Identifier, FuelType> FUEL_TYPES = new HashMap<>();

        /**
         * Represents a fuel type with a unique ID and associated fuel map.
         */
        public static class FuelType {
            private final Identifier id;
            private final Map<Identifier, Integer> fuelMap;

            public FuelType(Identifier id) {
                this.id = id;
                this.fuelMap = new HashMap<>();
            }

            public Identifier getId() {
                return id;
            }

            public Map<Identifier, Integer> getFuelMap() {
                return fuelMap;
            }
        }

        /**
         * Registers a new fuel type with a unique identifier.
         *
         * @param id the unique identifier for the fuel type (e.g., "yourmod:custom_lamp")
         * @return the registered FuelType instance
         */
        public static FuelType registerFuelType(Identifier id) {
            if (FUEL_TYPES.containsKey(id)) {
                LOGGER.warn("Fuel type {} already registered, returning existing instance", id);
                return FUEL_TYPES.get(id);
            }
            FuelType fuelType = new FuelType(id);
            FUEL_TYPES.put(id, fuelType);
            LOGGER.info("Registered fuel type {}", id);
            return fuelType;
        }

        /**
         * Gets the fuel type by its identifier.
         *
         * @param id the fuel type identifier
         * @return the FuelType instance, or null if not found
         */
        public static FuelType getFuelType(Identifier id) {
            return FUEL_TYPES.get(id);
        }

        /**
         * Gets the fuel burn time for an item in a specific fuel type (in ticks).
         *
         * @param fuelType the fuel type
         * @param itemId   the item identifier
         * @return the burn time in ticks, or 0 if not a valid fuel
         */
        public static long getFuelBurnTime(FuelType fuelType, Identifier itemId) {
            Integer seconds = fuelType.getFuelMap().get(itemId);
            return seconds != null ? seconds * 20L : 0;
        }

        /**
         * Gets all registered fuel type identifiers.
         *
         * @return set of registered fuel type IDs
         */
        public static Iterable<Identifier> getFuelTypeIds() {
            return FUEL_TYPES.keySet();
        }

        /**
         * Adds an item as a fuel for a specific fuel type with a burn time (in seconds).
         * <p>
         * This method is intended for <b>code-based registration</b>, allowing developers
         * to add fuels programmatically at runtime instead of relying solely on JSON/datapack definitions.
         * <p>
         * If the item is already registered for the given fuel type, the existing entry will be overwritten.
         *
         * @param fuelType        the fuel type to add the item to
         * @param item            the item to register as fuel
         * @param burnTimeSeconds the burn time in seconds (converted to ticks internally)
         */
        public static void addFuelItem(FuelType fuelType, Item item, int burnTimeSeconds) {
            Identifier itemId = net.minecraft.registry.Registries.ITEM.getId(item);
            if (fuelType.getFuelMap().containsKey(itemId)) {
                LOGGER.warn("Item {} already registered as fuel for {}, overwriting", itemId, fuelType.getId());
            }
            fuelType.getFuelMap().put(itemId, burnTimeSeconds);
            LOGGER.debug("Added fuel item {} for {} with {} seconds", itemId, fuelType.getId(), burnTimeSeconds);
        }

        /**
         * Adds an item tag as a fuel for a specific fuel type with a burn time (in seconds).
         * <p>
         * This method is intended for <b>code-based registration</b>, allowing developers
         * to add fuels for entire tags programmatically at runtime instead of relying solely
         * on JSON/datapack definitions.
         * <p>
         * Each item in the tag will be registered individually. If an item is already registered
         * for the given fuel type, the existing entry will be overwritten.
         *
         * @param fuelType        the fuel type to add the tag contents to
         * @param tag             the item tag whose contents should be registered as fuels
         * @param burnTimeSeconds the burn time in seconds (converted to ticks internally)
         */
        public static void addFuelTag(FuelType fuelType, TagKey<Item> tag, int burnTimeSeconds) {
            var entryList = net.minecraft.registry.Registries.ITEM.getEntryList(tag);
            if (entryList.isPresent()) {
                entryList.get().forEach(entry -> {
                    Identifier entryId = net.minecraft.registry.Registries.ITEM.getId(entry.value());
                    if (fuelType.getFuelMap().containsKey(entryId)) {
                        LOGGER.warn("Item {} from tag {} already registered for {}, overwriting", entryId, tag.id(), fuelType.getId());
                    }
                    fuelType.getFuelMap().put(entryId, burnTimeSeconds);
                    LOGGER.debug("Added fuel item {} from tag {} for {} with {} seconds", entryId, tag.id(), fuelType.getId(), burnTimeSeconds);
                });
            } else {
                LOGGER.warn("Tag {} is empty or invalid for fuel type {}", tag.id(), fuelType.getId());
            }
        }

        /**
         * Clears all registered fuel types.
         * Useful for datapack/resource reloads.
         */
        public static void clear() {
            for (FuelType type : FUEL_TYPES.values()) {
                type.getFuelMap().clear();
            }
            LOGGER.info("Cleared all fuel type contents");
        }
    }
