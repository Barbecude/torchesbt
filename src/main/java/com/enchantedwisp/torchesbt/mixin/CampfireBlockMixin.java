package com.enchantedwisp.torchesbt.mixin;

import com.enchantedwisp.torchesbt.core.FlameLevel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {

    @Unique
    private static final EnumProperty<FlameLevel> FLAME_LEVEL =
            EnumProperty.of("flame_level", FlameLevel.class);

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void appendFlameLevel(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(FLAME_LEVEL);
    }
}
