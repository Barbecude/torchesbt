package com.enchantedwisp.torchesbt.compat.trinkets;

import com.enchantedwisp.torchesbt.api.BurnTime;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.api.BurnTickEvents;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TrinketBurnHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("TrinketBurnHandler");

    public static void register() {
        BurnTime.registerPlayerItemTickHandler(TrinketBurnHandler::tickTrinkets);
    }

    public static void tickTrinkets(PlayerEntity player) {
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            for (var slot : component.getAllEquipped()) {
                ItemStack stack = slot.getRight();
                SlotReference slotRef = slot.getLeft();
                if (!BurnableRegistry.isBurnableItem(stack.getItem())) continue;

                long burnTime = BurnTimeUtils.getCurrentBurnTime(stack);
                if (burnTime <= 0) {
                    extinguishTrinket(player, component, slotRef, stack);
                    continue;
                }

                World world = player.getWorld();
                boolean isRaining = BurnTimeUtils.isActuallyRainingAt(world, player.getBlockPos());
                boolean isSubmerged = player.isSubmergedIn(net.minecraft.registry.tag.FluidTags.WATER);
                double rainMult = BurnableRegistry.getRainMultiplier(stack.getItem());
                double waterMult = BurnableRegistry.getWaterMultiplier(stack.getItem());

                double effectiveMultiplier = 1.0;
                if (isRaining) effectiveMultiplier = Math.max(effectiveMultiplier, rainMult);
                if (isSubmerged && waterMult > 0.0) effectiveMultiplier = Math.max(effectiveMultiplier, waterMult);

                long baseDecrement = (long) Math.ceil(effectiveMultiplier);
                BurnTickEvents.PlayerHeldContext heldContext = new BurnTickEvents.PlayerHeldContext(player, stack, baseDecrement);
                long finalDecrement = BurnTickEvents.PLAYER_HELD.invoker().onTick(heldContext, baseDecrement);
                burnTime -= finalDecrement;

                BurnTimeUtils.setCurrentBurnTime(stack, Math.max(0, burnTime));
                if (burnTime <= 0) extinguishTrinket(player, component, slotRef, stack);
            }
        });
    }

    private static void extinguishTrinket(PlayerEntity player, TrinketComponent component, SlotReference slotRef, ItemStack stack) {
        ItemStack unlit = new ItemStack(Objects.requireNonNull(BurnableRegistry.getUnlitItem(stack.getItem())), stack.getCount());
        String group = slotRef.inventory().getSlotType().getGroup();
        String slotName = slotRef.inventory().getSlotType().getName();
        component.getInventory().get(group).get(slotName).setStack(slotRef.index(), unlit);
        LOGGER.debug("Extinguished trinket item in slot {}/{} for player {}", group, slotName, player.getName().getString());
    }
}
