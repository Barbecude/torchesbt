package com.enchantedwisp.torchesbt.network;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.fuel.ItemFuelHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Defines the custom packet for refueling burnable items in the inventory or trinket slots.
 */
public record ItemFuelPacket(int handlerSlotId, ItemStack fuelStack) {
    public static final Identifier ID = new Identifier(RealisticTorchesBT.MOD_ID, "refuel_item");

    public ItemFuelPacket(int handlerSlotId, ItemStack fuelStack) {
        this.handlerSlotId = handlerSlotId;
        this.fuelStack = fuelStack.copy(); // Copy to avoid modifying client stack
    }

    // Encode data to send
    public void write(PacketByteBuf buf) {
        buf.writeInt(handlerSlotId);
        buf.writeItemStack(fuelStack);
    }

    // Decode data on server
    public static ItemFuelPacket read(PacketByteBuf buf) {
        int slotId = buf.readInt();
        ItemStack stack = buf.readItemStack();
        return new ItemFuelPacket(slotId, stack);
    }

    /**
     * Registers the server-side packet handler.
     */
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            ItemFuelPacket packet = ItemFuelPacket.read(buf);
            server.execute(() -> {
                // Handle refuel logic
                ItemFuelHandler.handleRefuel(player, packet.handlerSlotId(), packet.fuelStack());
            });
        });
    }
}