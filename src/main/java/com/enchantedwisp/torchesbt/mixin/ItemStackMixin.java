package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "isItemBarVisible", at = @At("HEAD"), cancellable = true)
    private void isItemBarVisible(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BurnableRegistry.isBurnableItem(stack.getItem())) {
            long max = BurnTimeUtils.getMaxBurnTime(stack);
            long current = BurnTimeUtils.getCurrentBurnTime(stack);
            cir.setReturnValue(current > 0 && current < max);
        }
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void getMaxDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BurnableRegistry.isBurnableItem(stack.getItem())) {
            cir.setReturnValue((int) BurnTimeUtils.getMaxBurnTime(stack));
        }
    }

    @Inject(method = "getDamage", at = @At("HEAD"), cancellable = true)
    private void getDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BurnableRegistry.isBurnableItem(stack.getItem())) {
            long max = BurnTimeUtils.getMaxBurnTime(stack);
            long current = BurnTimeUtils.getCurrentBurnTime(stack);
            cir.setReturnValue((int) (max - current));
        }
    }

    @Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
    private void getItemBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BurnableRegistry.isBurnableItem(stack.getItem())) {
            cir.setReturnValue(0xFFA500); // Orange
        }
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    private void getItemBarStep(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BurnableRegistry.isBurnableItem(stack.getItem())) {
            long max = BurnTimeUtils.getMaxBurnTime(stack);
            long current = BurnTimeUtils.getCurrentBurnTime(stack);
            cir.setReturnValue((int) (13.0 * current / max));
        }
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void getMaxCount(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BurnableRegistry.isBurnableItem(stack.getItem())) {
            if (!stack.hasNbt() || !Objects.requireNonNull(stack.getNbt()).contains("remaining_burn")) {
                cir.setReturnValue(stack.getItem().getMaxCount());
                return;
            }
            cir.setReturnValue(stack.getItem().getMaxCount());
        }
    }

    @Inject(method = "areEqual", at = @At("HEAD"), cancellable = true)
    private static void areEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if (BurnableRegistry.isBurnableItem(stack1.getItem()) && BurnableRegistry.isBurnableItem(stack2.getItem())) {
            // If items are the same, check their burn times
            if (ItemStack.areItemsEqual(stack1, stack2)) {
                long burnTime1 = BurnTimeUtils.getCurrentBurnTime(stack1);
                long burnTime2 = BurnTimeUtils.getCurrentBurnTime(stack2);
                cir.setReturnValue(burnTime1 == burnTime2);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}