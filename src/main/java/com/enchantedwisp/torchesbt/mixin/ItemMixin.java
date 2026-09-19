package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "appendTooltip", at = @At("TAIL"))
    private void appendBurnTimeTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (BurnableRegistry.isBurnableItem(stack.getItem()) && BurnableRegistry.isTickingEnabled(stack.getItem())) {
            long current = BurnTimeUtils.getCurrentBurnTime(stack);
            long max = BurnTimeUtils.getMaxBurnTime(stack);
            long curSec = current / 20;
            long maxSec = max / 20;
            tooltip.add(Text.literal(String.format("Burn Time: %d:%02d / %d:%02d (%ds)", curSec / 60, curSec % 60, maxSec / 60, maxSec % 60, curSec)));
        }
    }
}