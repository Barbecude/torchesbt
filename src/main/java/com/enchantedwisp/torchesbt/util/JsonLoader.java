package com.enchantedwisp.torchesbt.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads JSON-based configuration for igniters and fuels.
 * Supports custom fuel types via FuelTypeAPI.
 */
public class JsonLoader {
    public static final Map<Identifier, Integer> IGNITERS = new HashMap<>();
    private static final Logger LOGGER = com.enchantedwisp.torchesbt.RealisticTorchesBT.LOGGER;
    private static final Gson GSON = new Gson();

    public static void loadJsonFiles(ResourceManager manager, String folder, Map<Identifier, Integer> targetMap, String valueKey) {
        for (Identifier id : manager.findResources(folder, path -> path.getPath().endsWith(".json")).keySet()) {
            try {
                Resource resource = manager.getResourceOrThrow(id);
                try (InputStream inputStream = resource.getInputStream();
                     InputStreamReader reader = new InputStreamReader(inputStream)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    JsonArray items = json.getAsJsonArray("items");
                    int value = json.get(valueKey).getAsInt();

                    if (items == null || value <= 0) {
                        LOGGER.warn("Skipping invalid JSON at {}: Missing or invalid items/{}", id, valueKey);
                        continue;
                    }

                    LOGGER.debug("Processing JSON at {} with {} items and {}={}", id, items.size(), valueKey, value);
                    for (JsonElement itemElement : items) {
                        String itemString = itemElement.getAsString();
                        boolean isTag = itemString.startsWith("#");
                        String parsedString = isTag ? itemString.substring(1) : itemString;
                        Identifier itemId = Identifier.tryParse(parsedString);

                        if (itemId == null) {
                            LOGGER.warn("Invalid identifier in JSON at {}: {}", id, itemString);
                            continue;
                        }

                        if (isTag) {
                            TagKey<net.minecraft.item.Item> tag = TagKey.of(RegistryKeys.ITEM, itemId);
                            var entryList = Registries.ITEM.getEntryList(tag);
                            if (entryList.isPresent()) {
                                entryList.get().forEach(entry -> {
                                    Identifier entryId = Registries.ITEM.getId(entry.value());
                                    putIfAbsent(entryId, value, targetMap, folder);
                                    LOGGER.debug("Added tag item {} from {} with {}={}", entryId, itemId, valueKey, value);
                                });
                            } else {
                                LOGGER.warn("Tag {} in JSON at {} is empty or invalid", itemId, id);
                            }
                        } else {
                            if (Registries.ITEM.containsId(itemId)) {
                                Identifier entryId = Registries.ITEM.getId(Registries.ITEM.get(itemId));
                                putIfAbsent(entryId, value, targetMap, folder);
                                LOGGER.debug("Added item {} with {}={}", entryId, valueKey, value);
                            } else {
                                LOGGER.warn("Unknown item {} in JSON at {}", itemId, id);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load JSON at {}: {}", id, e.getMessage());
            }
        }
        LOGGER.info("Loaded {} entries from {} JSONs", targetMap.size(), folder);
    }

    private static void putIfAbsent(Identifier entryId, int value, Map<Identifier, Integer> targetMap, String folder) {
        if (!targetMap.containsKey(entryId)) {
            targetMap.put(entryId, value);
        } else {
            LOGGER.warn("Duplicate item {} in {} JSON, keeping first value", entryId, folder);
        }
    }
}