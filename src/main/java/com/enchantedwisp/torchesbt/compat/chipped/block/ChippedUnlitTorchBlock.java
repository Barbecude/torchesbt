package com.enchantedwisp.torchesbt.compat.chipped.block;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.registry.Particles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ChippedUnlitTorchBlock extends TorchBlock {
    private final String variant;

    public ChippedUnlitTorchBlock(String variant) {
        super(Settings.copy(Blocks.TORCH)
                        .luminance(state -> 0)
                        .sounds(BlockSoundGroup.WOOD),
                Particles.TORCH_SMOKE);
        this.variant = variant;
    }

    @Override
    public String getTranslationKey() {
        return "block." + RealisticTorchesBT.MOD_ID + ".chipped.unlit_" + variant + "_torch";
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isClient()) return;

        if (random.nextFloat() < 0.125F) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.75;
            double z = pos.getZ() + 0.5;

            double vx = (random.nextDouble() - 0.5) * 0.02;
            double vy = random.nextDouble() * 0.02;
            double vz = (random.nextDouble() - 0.5) * 0.02;

            world.addParticle(Particles.TORCH_SMOKE, x, y, z, vx, vy, vz);
        }
    }
}