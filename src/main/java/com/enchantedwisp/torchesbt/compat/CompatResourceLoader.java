package com.enchantedwisp.torchesbt.compat;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

/**
 * Manages registration of compatibility resource packs for supported mods.
 * Only logs when a mod is found and its resource pack is successfully registered.
 */
public class CompatResourceLoader {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    // Record to hold mod compatibility details
    private record ModCompat(String modId, String packName, String packDisplayName) {
        Identifier getPackId() {
            return new Identifier(RealisticTorchesBT.MOD_ID, packName);
        }

        Text getDisplayName() {
            return Text.literal(packDisplayName + " Compat for RealisticTorchesBT");
        }
    }

    private static final ModCompat[] COMPAT_MODS = {
            new ModCompat(
                    "chipped",
                    "chipped_compat",
                    "Chipped"),
            new ModCompat(
                    "trinkets",
                    "trinkets_compat",
                    "Trinkets")
    };

    public static void register() {
        ModContainer container = FabricLoader.getInstance()
                .getModContainer(RealisticTorchesBT.MOD_ID)
                .orElse(null);
        if (container == null) {
            LOGGER.warn("[Compat] Could not find mod container, skipping all compatibility resource packs");
            return;
        }

        for (ModCompat mod : COMPAT_MODS) {
            if (FabricLoader.getInstance().isModLoaded(mod.modId)) {
                boolean success = ResourceManagerHelper.registerBuiltinResourcePack(
                        mod.getPackId(),
                        container,
                        mod.getDisplayName(),
                        ResourcePackActivationType.ALWAYS_ENABLED
                );
                if (success) {
                    LOGGER.info("[Compat] {} - Resource pack for RealisticTorchesBT registered!", mod.packDisplayName);
                } else {
                    LOGGER.warn("[Compat] Failed to register {} resource pack!", mod.packDisplayName);
                }
            }
        }
    }
}