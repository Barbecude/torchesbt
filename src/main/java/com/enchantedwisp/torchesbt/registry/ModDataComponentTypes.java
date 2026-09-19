package com.enchantedwisp.torchesbt.registry;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModDataComponentTypes {
    public static final ComponentType<Long> REMAINING_BURN = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(RealisticTorchesBT.MOD_ID, "remaining_burn"),
            ComponentType.<Long>builder()
                    .codec(Codec.LONG)
                    .packetCodec(PacketCodecs.VAR_LONG)
                    .build()
    );

    public static void register() {
        RealisticTorchesBT.LOGGER.info("Registering data component types for {}", RealisticTorchesBT.MOD_ID);
    }
}
