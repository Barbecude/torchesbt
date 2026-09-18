package com.enchantedwisp.torchesbt.registry.blocks;

import com.enchantedwisp.torchesbt.registry.Particles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UnlitLanternBlock extends LanternBlock {
    public UnlitLanternBlock() {
        super(Settings.copy(Blocks.LANTERN)
                .luminance(state -> 0));
    }

    @Override
    public String getTranslationKey() {
        return "block.torchesbt.unlit_lantern";
    }

    // Client-side particle spawn
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isClient()) return;

        // Emit only occasionally (e.g., 1 in 8 ticks)
        if (random.nextInt(8) != 0) return;

        ClientWorld clientWorld = (ClientWorld) world;

        double x = pos.getX() + 0.5;
        double y;
        double z = pos.getZ() + 0.5;

        boolean hanging = state.get(LanternBlock.HANGING);

        if (hanging) {
            y = pos.getY() + 0.28; // hanging lantern
        } else {
            y = pos.getY() + 0.22; // standing lantern
        }

        // Small random motion for natural smoke
        double vx = (random.nextDouble() - 0.5) * 0.02;
        double vy = random.nextDouble() * 0.02;
        double vz = (random.nextDouble() - 0.5) * 0.02;

        clientWorld.addParticle(Particles.CANDLE_SMOKE, x, y, z, vx, vy, vz);
    }
}
