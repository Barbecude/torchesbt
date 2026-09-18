package com.enchantedwisp.torchesbt.compat.chipped.block;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.registry.Particles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ChippedUnlitWallTorchBlock extends WallTorchBlock {
    private final String variant;

    public ChippedUnlitWallTorchBlock(String variant) {
        super(Settings.copy(Blocks.WALL_TORCH)
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
        if (random.nextFloat() < 0.25f) {
            Direction direction = state.get(FACING);
            double d = pos.getX() + 0.5;
            double e = pos.getY() + 0.7;
            double f = pos.getZ() + 0.5;

            Direction direction2 = direction.getOpposite();
            world.addParticle(
                    Particles.TORCH_SMOKE,
                    d + 0.27 * direction2.getOffsetX(),
                    e + 0.22,
                    f + 0.27 * direction2.getOffsetZ(),
                    0.0F, 0.0F, 0.0F
            );
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(Registries.ITEM.get(Identifier.of(RealisticTorchesBT.MOD_ID, "chipped.unlit_" + variant.replace("_wall_torch", "_torch"))));
    }
}