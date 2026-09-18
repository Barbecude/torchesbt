package com.enchantedwisp.torchesbt.datagen;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
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

@SuppressWarnings("UnstableApiUsage")
public class CompatLootTableProvider extends FabricBlockLootTableProvider {
    private static final String[] CHIPPED_LANTERNS = {
            "blue_paper", "burning_coal", "checkered_iron", "dark_blue_paper",
            "ender", "green_paper", "iron_bowl", "purple_paper",
            "red_paper", "small_green", "white_paper", "wooden_cage",
            "wrought_iron", "yellow_tube"
    };

    private static final String[] SPECIAL_LANTERNS = {
            "big", "donut", "tall", "wide"
    };

    private static final String[] CHIPPED_TORCHES = {
            "acacia", "birch", "crimson", "dark_oak",
            "glow", "iron", "jungle", "spruce", "warped"
    };

    @Override
    public String getName() {
        return "Realistic Torches BT/Compat Loot Tables for minecraft:block";
    }

    public CompatLootTableProvider(FabricDataOutput baseOutput) {
        super(new FabricDataOutput(
                baseOutput.getModContainer(),
                baseOutput.getPath().resolve("resourcepacks").resolve("chipped_compat"),
                baseOutput.isStrictValidationEnabled()
        ));
    }

    @Override
    public void generate() {
        // Chipped unlit lanterns
        for (String variant : CHIPPED_LANTERNS) {
            addLanternVariant(variant);
        }
        for (String variant : SPECIAL_LANTERNS) {
            addLanternVariant(variant);
        }

        // Chipped unlit torches
        for (String variant : CHIPPED_TORCHES) {
            Block unlitTorchBlock = Registries.BLOCK.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_torch"));
            Block unlitWallTorchBlock = Registries.BLOCK.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_wall_torch"));
            Item unlitItem = Registries.ITEM.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_torch"));
            addDrop(unlitTorchBlock, unlitItem);
            addDrop(unlitWallTorchBlock, unlitItem);
        }
    }

    private void addLanternVariant(String variant) {
        Block unlitBlock = Registries.BLOCK.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_lantern"));
        Item unlitItem = Registries.ITEM.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_lantern"));
        addDrop(unlitBlock, unlitItem);
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
