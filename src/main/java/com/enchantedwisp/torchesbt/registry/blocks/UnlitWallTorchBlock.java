package com.enchantedwisp.torchesbt.registry.blocks;


import com.enchantedwisp.torchesbt.registry.Particles;
import com.enchantedwisp.torchesbt.registry.RegistryHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class UnlitWallTorchBlock extends WallTorchBlock {
    public UnlitWallTorchBlock() {
        super(Settings.copy(Blocks.WALL_TORCH)
                        .luminance(state -> 0) // No light emission
                        .sounds(BlockSoundGroup.WOOD),
                Particles.TORCH_SMOKE);
    }

    @Override
    public String getTranslationKey() {
        return "block.torchesbt.unlit_wall_torch";
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // 25% chance to spawn (adjust to taste)
        if (random.nextFloat() < 0.25f) {
            Direction direction = state.get(FACING);
            double d = (double) pos.getX() + 0.5;
            double e = (double) pos.getY() + 0.7;
            double f = (double) pos.getZ() + 0.5;

            Direction direction2 = direction.getOpposite();
            world.addParticle(
                    Particles.TORCH_SMOKE,
                    d + 0.27 * (double) direction2.getOffsetX(),
                    e + 0.22,
                    f + 0.27 * (double) direction2.getOffsetZ(),
                    0.0F, 0.0F, 0.0F
            );
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(RegistryHandler.UNLIT_TORCH);
    }
}
