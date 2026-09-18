package com.enchantedwisp.torchesbt.datagen;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.registry.RegistryHandler;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        addDrop(RegistryHandler.UNLIT_TORCH_BLOCK, RegistryHandler.UNLIT_TORCH);
        addDrop(RegistryHandler.UNLIT_WALL_TORCH_BLOCK, RegistryHandler.UNLIT_TORCH);
        addDrop(RegistryHandler.UNLIT_LANTERN_BLOCK, RegistryHandler.UNLIT_LANTERN);
    }

    private void addDrop(Block block, Item item) {
        addDrop(block, LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(item))
                        .conditionally(SurvivesExplosionLootCondition.builder())
                )
        );
    }
}