package com.enchantedwisp.torchesbt.network;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.fuel.ItemFuelHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Defines the custom packet for refueling burnable items in the inventory or trinket slots.
 */
public record ItemFuelPacket(int handlerSlotId, ItemStack fuelStack) implements CustomPayload {
    public static final CustomPayload.Id<ItemFuelPacket> ID =
            new CustomPayload.Id<>(Identifier.of(RealisticTorchesBT.MOD_ID, "refuel_item"));

    public static final PacketCodec<RegistryByteBuf, ItemFuelPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ItemFuelPacket::handlerSlotId,
            ItemStack.PACKET_CODEC, ItemFuelPacket::fuelStack,
            ItemFuelPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Registers the payload type and server-side packet handler.
     */
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            context.server().execute(() -> {
                ItemFuelHandler.handleRefuel(context.player(), payload.handlerSlotId(), payload.fuelStack());
            });
        });
    }
}