package com.enchantedwisp.torchesbt.core;

import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import static com.enchantedwisp.torchesbt.RealisticTorchesBT.LOGGER;
import static com.enchantedwisp.torchesbt.util.JsonLoader.loadJsonFiles;

/**
 * Handles loading of all non-default fuel types (from other mods or custom datapacks).
 */
public class CustomFuelTypes {
    /**
     * Loads custom fuel type definitions from JSON files.
     *
     * @param manager Resource manager to pull JSONs from
     */
    public static void load(ResourceManager manager) {
        for (Identifier fuelTypeId : FuelTypeAPI.getFuelTypeIds()) {
            if (!fuelTypeId.getNamespace().equals("torchesbt")) {
                loadJsonFiles(
                        manager,
                        "fuel/" + fuelTypeId.getPath(),
                        FuelTypeAPI.getFuelType(fuelTypeId).getFuelMap(),
                        "add_time"
                );
                LOGGER.info("Loaded custom fuel type: {}", fuelTypeId);
            }
        }
    }

    private CustomFuelTypes() {
    }
}
