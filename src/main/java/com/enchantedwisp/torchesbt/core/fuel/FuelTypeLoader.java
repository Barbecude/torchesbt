package com.enchantedwisp.torchesbt.core.fuel;

import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import com.enchantedwisp.torchesbt.core.CustomFuelTypes;
import com.enchantedwisp.torchesbt.core.DefaultFuelTypes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import static com.enchantedwisp.torchesbt.util.JsonLoader.IGNITERS;
import static com.enchantedwisp.torchesbt.util.JsonLoader.loadJsonFiles;

/**
 * Central class for registering and reloading all fuel types (default + custom).
 */
public class FuelTypeLoader {
    private static final Logger LOGGER = com.enchantedwisp.torchesbt.RealisticTorchesBT.LOGGER;

    /** Tracks whether the server is still starting to prevent double reload */
    private static boolean serverStarting = true;

    private static void reset() {
        IGNITERS.clear();
        FuelTypeAPI.clear();
        LOGGER.debug("Reset fuel and igniter registries");
    }

    /** Load all fuels and igniters from JSON */
    private static void loadAll(ResourceManager manager) {
        // Load igniters
        loadJsonFiles(manager, "ignite", IGNITERS, "ignite_amount");

        // Load default fuel types
        loadJsonFiles(manager, "fuel/torch", DefaultFuelTypes.TORCH.getFuelMap(), "add_time");
        loadJsonFiles(manager, "fuel/lantern", DefaultFuelTypes.LANTERN.getFuelMap(), "add_time");
        loadJsonFiles(manager, "fuel/campfire", DefaultFuelTypes.CAMPFIRE.getFuelMap(), "add_time");

        // Load custom fuels
        CustomFuelTypes.load(manager);
    }

    public static void register() {
        // Load fuels when the server starts
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ResourceManager manager = server.getResourceManager();
            LOGGER.info("Server starting, loading fuels and igniters");
            reset();
            loadAll(manager);
            LOGGER.info("Finished loading all fuels and igniters");
        });

        // Register reload listener for manual /reloads
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    private final Identifier RELOAD_ID = new Identifier("torchesbt", "json_loader");

                    @Override
                    public Identifier getFabricId() {
                        return RELOAD_ID;
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        if (serverStarting) {
                            // Skip the automatic reload during server startup
                            serverStarting = false;
                            return;
                        }

                        LOGGER.info("Reloading fuels and igniters");
                        reset();
                        loadAll(manager);
                        LOGGER.info("Finished reloading all fuels and igniters");
                    }
                });

        LOGGER.info("Registered fuel type loader");
    }

    private FuelTypeLoader() {
    }
}
