package com.enchantedwisp.torchesbt.mixinaccess;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface ICampfireBurnAccessor {
    long torchesbt_getBurnTime();
    void torchesbt_setBurnTime(long time);
    DefaultedList<ItemStack> torchesbt_getItems();
}
