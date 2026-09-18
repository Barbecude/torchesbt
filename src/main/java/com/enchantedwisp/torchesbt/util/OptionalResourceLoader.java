package com.enchantedwisp.torchesbt.util;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OptionalResourceLoader {

    public static void loadOptionalResources() {
        ModContainer container = FabricLoader.getInstance()
                .getModContainer(RealisticTorchesBT.MOD_ID)
                .orElse(null);

        if (container == null) return;

        Identifier packId = new Identifier(RealisticTorchesBT.MOD_ID, "empty_lanterns");

        ResourceManagerHelper.registerBuiltinResourcePack(
                packId,
                container,
                Text.literal("RealisticTorchesBT: Optional Empty Lanterns"),
                ResourcePackActivationType.NORMAL
        );
    }
}
