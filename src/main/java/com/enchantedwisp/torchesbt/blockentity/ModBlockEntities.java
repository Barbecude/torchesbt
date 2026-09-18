package com.enchantedwisp.torchesbt.blockentity;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<TorchBlockEntity> TORCH_BLOCK_ENTITY =
            BlockEntityType.Builder.create(TorchBlockEntity::new, Blocks.TORCH, Blocks.WALL_TORCH).build(null);

    public static final BlockEntityType<LanternBlockEntity> LANTERN_BLOCK_ENTITY =
            BlockEntityType.Builder.create(LanternBlockEntity::new, Blocks.LANTERN).build(null);

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(RealisticTorchesBT.MOD_ID, "torch_block_entity"), TORCH_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(RealisticTorchesBT.MOD_ID, "lantern_block_entity"), LANTERN_BLOCK_ENTITY);
    }
}