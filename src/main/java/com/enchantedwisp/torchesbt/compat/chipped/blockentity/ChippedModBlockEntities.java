package com.enchantedwisp.torchesbt.compat.chipped.blockentity;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.compat.chipped.ChippedRegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Handles registration of the special Chipped lantern BlockEntity.
 * Waits until all four lantern variants ("big", "donut", "tall", "wide")
 * are detected before registering.
 */
public class ChippedModBlockEntities {
    private static final String[] SPECIAL_LANTERNS = { "big", "donut", "tall", "wide" };

    public static BlockEntityType<SpecialLanternBlockEntity> SPECIAL_LANTERN_BLOCK_ENTITY;

    // Tracks which lanterns have been found
    private static final Map<String, Block> foundLanterns = new HashMap<>();

    /**
     * Called when a Chipped lantern block is detected in the registry.
     *
     * @param variant The lantern variant (big, donut, tall, wide)
     * @param block   The registered block instance
     */
    public static void onLanternDetected(String variant, Block block) {
        if (SPECIAL_LANTERN_BLOCK_ENTITY != null) {
            return; // Already registered
        }

        foundLanterns.put(variant, block);

        // Check if all required lanterns are present
        if (foundLanterns.keySet().containsAll(Arrays.asList(SPECIAL_LANTERNS))) {
            RealisticTorchesBT.LOGGER.info("[Compat] All special Chipped lanterns found, registering BlockEntityType");

            SPECIAL_LANTERN_BLOCK_ENTITY = BlockEntityType.Builder
                    .create(SpecialLanternBlockEntity::new, foundLanterns.values().toArray(new Block[0]))
                    .build(null);

            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(RealisticTorchesBT.MOD_ID, "special_lantern_block_entity"),
                    SPECIAL_LANTERN_BLOCK_ENTITY
            );
            ChippedRegistryHandler.registerBurnables();
        }
    }
}
