package com.enchantedwisp.torchesbt.client;

import com.enchantedwisp.torchesbt.compat.CompatRegistryHandler;
import com.enchantedwisp.torchesbt.registry.Particles;
import com.enchantedwisp.torchesbt.registry.RegistryHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;


public class RealisticTorchesBTClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register Particles
        Particles.registerClient();
        CompatRegistryHandler.registerChippedClient();

        // Render Blocks Correctly
        BlockRenderLayerMap.INSTANCE.putBlock(Block.getBlockFromItem(RegistryHandler.UNLIT_TORCH), RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(RegistryHandler.UNLIT_WALL_TORCH_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Block.getBlockFromItem(RegistryHandler.UNLIT_LANTERN), RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(RegistryHandler.UNLIT_LANTERN_BLOCK, RenderLayer.getCutout());
    }
}
