package com.enchantedwisp.torchesbt;

import com.enchantedwisp.torchesbt.blockentity.ModBlockEntities;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeManager;
import com.enchantedwisp.torchesbt.compat.CompatRegistryHandler;
import com.enchantedwisp.torchesbt.compat.CompatResourceLoader;
import com.enchantedwisp.torchesbt.core.fuel.FuelTypeLoader;
import com.enchantedwisp.torchesbt.core.fuel.ItemFuelHandler;
import com.enchantedwisp.torchesbt.core.ignition.ReignitionHandler;
import com.enchantedwisp.torchesbt.config.RealisticTorchesBTConfig;
import com.enchantedwisp.torchesbt.network.ItemFuelPacket;
import com.enchantedwisp.torchesbt.registry.Particles;
import com.enchantedwisp.torchesbt.registry.RegistryHandler;
import com.enchantedwisp.torchesbt.util.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealisticTorchesBT implements ModInitializer {
    public static final String MOD_ID = "torchesbt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ConfigHolder<RealisticTorchesBTConfig> configHolder;
    private static RealisticTorchesBTConfig config;

    public static RealisticTorchesBTConfig getConfig() {
        return config;
    }

    public static void saveConfig() {
        try {
            configHolder.save();
        } catch (Exception e) {
            LOGGER.error("Failed to save RealisticTorchesBTConfig to config/torchesbt.json5", e);
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Realistic Torches BT");
        OptionalResourceLoader.loadOptionalResources();

        // Register config
        try {
            configHolder = AutoConfig.register(RealisticTorchesBTConfig.class, JanksonConfigSerializer::new);
            config = configHolder.getConfig();
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize RealisticTorchesBTConfig from config/torchesbt.json5, falling back to defaults", e);
            config = new RealisticTorchesBTConfig();
            configHolder = AutoConfig.getConfigHolder(RealisticTorchesBTConfig.class);
        }

        // Initialize ConfigCache
        ConfigCache.initialize();

        // Fuels
        FuelTypeLoader.register();
        ItemFuelPacket.register();

        // Register items
        RegistryHandler.register();
        ModBlockEntities.register();
        RegistryHandler.registerBurnables();


        // Compat
        CompatRegistryHandler.registerChipped();
        CompatRegistryHandler.registerTrinkets();
        CompatResourceLoader.register();


        // Register Particle
        Particles.register();

        // Burntime
        ReignitionHandler.register();
        BurnTimeManager.register();
    }
}