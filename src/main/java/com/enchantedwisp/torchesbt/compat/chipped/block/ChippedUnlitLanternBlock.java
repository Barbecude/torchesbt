package com.enchantedwisp.torchesbt.compat.chipped.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;

public class ChippedUnlitLanternBlock extends LanternBlock {
    public ChippedUnlitLanternBlock() {
        super(Settings.copy(Blocks.LANTERN)
                .luminance(state -> 0));
    }

}