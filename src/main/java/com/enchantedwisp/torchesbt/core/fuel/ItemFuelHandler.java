package com.enchantedwisp.torchesbt.core.fuel;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.util.ConfigCache;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;

/**
 * Handles refueling of burnable items in the player's screen handler slots (inventory or trinkets).
 * Processes custom packets sent when a player left-clicks a burnable item with a fuel item.
 */
public class ItemFuelHandler {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    public static void register() {
        LOGGER.info("Registered item fuel handler");
    }

    public static void handleRefuel(PlayerEntity player, int handlerSlotId, ItemStack fuelStack) {
        if (player.getWorld().isClient) return;

        // Check if inventory refueling is enabled
        if (!ConfigCache.allowInventoryRefueling()) {
            LOGGER.debug("Inventory refueling disabled in config, ignoring refuel request");
            return;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        ScreenHandler screenHandler = serverPlayer.currentScreenHandler;

        // Validate slot
        if (handlerSlotId < 0 || handlerSlotId >= screenHandler.slots.size()) return;
        Slot slot = screenHandler.getSlot(handlerSlotId);
        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty() || !BurnableRegistry.isBurnableItem(targetStack.getItem())) return;
        // Only allow refueling single items
        if (targetStack.getCount() > 1) return;

        // Validate fuel
        Identifier fuelItemId = Registries.ITEM.getId(fuelStack.getItem());
        FuelTypeAPI.FuelType fuelType = BurnableRegistry.getFuelType(Block.getBlockFromItem(targetStack.getItem()));
        if (fuelType == null || !fuelType.getFuelMap().containsKey(fuelItemId)) return;

        long currentBurnTime = BurnTimeUtils.getCurrentBurnTime(targetStack);
        long maxBurnTime = BurnableRegistry.getBurnTime(targetStack.getItem());
        long addedBurnTime = FuelTypeAPI.getFuelBurnTime(fuelType, fuelItemId);
        long newBurnTime = Math.min(currentBurnTime + addedBurnTime, maxBurnTime);

        if (newBurnTime == currentBurnTime) return; // Already full

        World world = player.getWorld();

        // Update burn time (modifies the stack in place)
        BurnTimeUtils.setCurrentBurnTime(targetStack, newBurnTime);

        // Sync the updated slot to the client
        serverPlayer.networkHandler.sendPacket(
                new ScreenHandlerSlotUpdateS2CPacket(
                        screenHandler.syncId,
                        screenHandler.getRevision(),
                        handlerSlotId,
                        targetStack.copy()
                )
        );

        // Consume or damage fuel
        if (fuelStack.isDamageable()) {
            fuelStack.damage(1, player, p -> {});
            if (fuelStack.isEmpty()) {
                screenHandler.setCursorStack(ItemStack.EMPTY);
                serverPlayer.networkHandler.sendPacket(
                        new ScreenHandlerSlotUpdateS2CPacket(-1, screenHandler.getRevision(), -1, ItemStack.EMPTY)
                );
            } else {
                screenHandler.setCursorStack(fuelStack);
                serverPlayer.networkHandler.sendPacket(
                        new ScreenHandlerSlotUpdateS2CPacket(-1, screenHandler.getRevision(), -1, fuelStack.copy())
                );
            }
        } else {
            fuelStack.decrement(1);
            screenHandler.setCursorStack(fuelStack);
            serverPlayer.networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(-1, screenHandler.getRevision(), -1, fuelStack.copy())
            );
        }

        // Play sound
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 0.8f, 0.6f);
    }
}