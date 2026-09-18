package com.enchantedwisp.torchesbt.integration;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.burn.Burnable;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Integration with Jade to display live burn time tooltips for burnable blocks.
 */
public class JadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new IBlockComponentProvider() {

            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                Block block = accessor.getBlock();
                BlockEntity entity = accessor.getBlockEntity();

                // Skip if not burnable
                if (!BurnableRegistry.isBurnableBlock(block)) return;

                // Skip if ticking is disabled for this block type (burn time is infinite/vanilla)
                if (!BurnableRegistry.isTickingEnabled(block)) return;

                // For campfires, only show if lit
                if (block == Blocks.CAMPFIRE && !accessor.getBlockState().get(net.minecraft.block.CampfireBlock.LIT)) return;

                long burnTime = getCurrentBurnTime(block, entity);
                long maxBurnTime = getMaxBurnTime(block, entity);

                if (burnTime > 0 && maxBurnTime > 0) {
                    tooltip.add(Text.literal("Burn Time: " + (burnTime / 20) + "/" + (maxBurnTime / 20)));
                }
            }

            private long getCurrentBurnTime(Block block, BlockEntity entity) {
                if (entity instanceof Burnable burnable) {
                    return burnable.getRemainingBurnTime();
                } else if (entity instanceof ICampfireBurnAccessor campfire) {
                    return campfire.torchesbt_getBurnTime();
                } else if (BurnableRegistry.isBurnableBlock(block)) {
                    // fallback for blocks without an entity
                    return BurnableRegistry.getBurnTime(block);
                }
                return 0;
            }

            private long getMaxBurnTime(Block block, BlockEntity entity) {
                if (entity instanceof Burnable burnable) {
                    return burnable.getMaxBurnTime();
                } else if (entity instanceof ICampfireBurnAccessor) {
                    return BurnableRegistry.getBurnTime(block);
                } else {
                    return BurnableRegistry.getBurnTime(block);
                }
            }

            @Override
            public Identifier getUid() {
                return new Identifier(RealisticTorchesBT.MOD_ID, "burn_time");
            }

        }, Block.class);
    }
}
