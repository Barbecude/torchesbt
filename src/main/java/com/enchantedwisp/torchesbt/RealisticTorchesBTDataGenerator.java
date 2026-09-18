package com.enchantedwisp.torchesbt;

import com.enchantedwisp.torchesbt.datagen.CompatBlockTagProvider;
import com.enchantedwisp.torchesbt.datagen.CompatLootTableProvider;
import com.enchantedwisp.torchesbt.datagen.ModBlockTagProvider;
import com.enchantedwisp.torchesbt.datagen.ModLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class RealisticTorchesBTDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack mainPack = generator.createPack();
        mainPack.addProvider(ModLootTableProvider::new);
        mainPack.addProvider(ModBlockTagProvider::new);

        FabricDataGenerator.Pack compatPack = generator.createPack();
        compatPack.addProvider(CompatBlockTagProvider::new);
        compatPack.addProvider(CompatLootTableProvider::new);
    }
}
