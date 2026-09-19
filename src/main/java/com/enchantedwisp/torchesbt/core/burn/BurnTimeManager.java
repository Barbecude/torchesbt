package com.enchantedwisp.torchesbt.core.burn;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.api.BurnTickEvents;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.util.ConfigCache;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

import static com.enchantedwisp.torchesbt.core.ignition.IgnitionHandler.copyProperties;

/**
 * Handles burn time ticking for player-held items, equipped trinkets, nearby dropped items, and burnable blocks.
 * Only ticks items when Dynamic Lights is enabled.
 * Fires BurnTickEvents to allow external mods to modify burn time decrements.
 */
public class BurnTimeManager {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;
    private static final int ITEM_UPDATE_INTERVAL = 5;
    private static final int SCAN_RADIUS = 32;
    private static final int SCAN_VERTICAL = 8;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % ITEM_UPDATE_INTERVAL != 0) return;

            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                processPlayerItems(player);
                processNearbyBurnables(player);
            }
        });

        LOGGER.info("Registered burn time tick handler");
    }

    // --- Tick player-held items ---
    private static void processPlayerItems(PlayerEntity player) {
        if (player.getWorld().isClient) return;

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (!BurnableRegistry.isBurnableItem(stack.getItem())) continue;

            if (!BurnableRegistry.isTickingEnabled(stack.getItem())) continue;

            long burnTime = BurnTimeUtils.getCurrentBurnTime(stack);
            if (burnTime <= 0) {
                extinguishPlayerItem(player, hand, stack);
                continue;
            }

            World world = player.getWorld();
            BlockPos pos = player.getBlockPos();
            boolean isRainingOrSnowing = BurnTimeUtils.isActuallyRainingAt(world, pos);
            boolean isSubmerged = player.isSubmergedIn(FluidTags.WATER);
            double rainMult = BurnableRegistry.getRainMultiplier(stack.getItem());
            double waterMult = BurnableRegistry.getWaterMultiplier(stack.getItem());

            if (isSubmerged && waterMult == 10.0) {
                burnTime = 0;
                LOGGER.debug("Instantly extinguished held item due to water submersion (multiplier=10)");
            } else {
                double effectiveMultiplier = 1.0;
                boolean isTorch = BurnTimeUtils.isTorch(stack.getItem());

                if (isRainingOrSnowing) {
                    if (isTorch) {
                        // Torch held in hand or off hand dies in 30 seconds when raining or snowing (6x burn rate for 180s base)
                        effectiveMultiplier = Math.max(effectiveMultiplier, 6.0);
                    } else {
                        effectiveMultiplier = Math.max(effectiveMultiplier, rainMult);
                    }
                }
                if (isSubmerged && waterMult > 0.0) {  // Ignore if <=0
                    effectiveMultiplier = Math.max(effectiveMultiplier, waterMult);
                }

                // Decrement burn time based on elapsed ticks since last update (ITEM_UPDATE_INTERVAL = 5 ticks)
                long elapsedTicks = ITEM_UPDATE_INTERVAL;
                long baseDecrement = (long) Math.ceil(elapsedTicks * effectiveMultiplier);
                BurnTickEvents.PlayerHeldContext heldContext = new BurnTickEvents.PlayerHeldContext(player, stack, baseDecrement);
                long finalDecrement = BurnTickEvents.PLAYER_HELD.invoker().onTick(heldContext, baseDecrement);
                burnTime -= finalDecrement;
            }

            BurnTimeUtils.setCurrentBurnTime(stack, Math.max(0, burnTime));

            if (burnTime <= 0) {
                extinguishPlayerItem(player, hand, stack);
            }
        }
        // Call registered player item tick handlers
        com.enchantedwisp.torchesbt.api.BurnTime.runPlayerItemTickHandlers(player);
    }

    private static void extinguishPlayerItem(PlayerEntity player, Hand hand, ItemStack stack) {
        net.minecraft.item.Item unlitItem = BurnableRegistry.getUnlitItem(stack.getItem());
        if (unlitItem == null) return;

        World world = player.getWorld();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH,
                net.minecraft.sound.SoundCategory.PLAYERS, 0.5f, 1.2f);

        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    net.minecraft.particle.ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    8, 0.1, 0.1, 0.1, 0.02
            );
        }

        if (stack.getCount() > 1) {
            stack.decrement(1);
            BurnTimeUtils.setCurrentBurnTime(stack, BurnTimeUtils.getMaxBurnTime(stack));
            ItemStack unlit = new ItemStack(unlitItem, 1);
            if (!player.getInventory().insertStack(unlit)) {
                player.dropItem(unlit, false);
            }
        } else {
            ItemStack unlit = new ItemStack(unlitItem, 1);
            player.setStackInHand(hand, unlit);
        }
    }


    // --- Tick nearby burnables ---
    private static void processNearbyBurnables(PlayerEntity player) {
        World world = player.getWorld();
        if (world.isClient) return;

        BlockPos pos = player.getBlockPos();
        Box scanBox = new Box(pos).expand(SCAN_RADIUS, SCAN_VERTICAL, SCAN_RADIUS);

        // Blocks
        for (BlockPos blockPos : BlockPos.iterate(pos.add(-SCAN_RADIUS, -SCAN_VERTICAL, -SCAN_RADIUS),
                pos.add(SCAN_RADIUS, SCAN_VERTICAL, SCAN_RADIUS))) {
            BlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();
            BlockEntity entity = world.getBlockEntity(blockPos);

            if (entity instanceof Burnable burnable) {
                tickBurnableBlock(world, blockPos, state, block, burnable);
            } else if (entity instanceof ICampfireBurnAccessor campfire) {
                tickCampfire(world, blockPos, state, campfire);
            }
        }

        // Dropped items
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, scanBox, e -> BurnableRegistry.isBurnableItem(e.getStack().getItem()));
        for (ItemEntity itemEntity : items) {
            if (!ConfigCache.isDynamicLightsEnabled()) continue;

            ItemStack stack = itemEntity.getStack();
            long burnTime = BurnTimeUtils.getCurrentBurnTime(stack);
            if (burnTime <= 0) {
                ItemStack unlit = new ItemStack(Objects.requireNonNull(BurnableRegistry.getUnlitItem(stack.getItem())), stack.getCount());
                itemEntity.setStack(unlit);
                continue;
            }

            if (!BurnableRegistry.isTickingEnabled(stack.getItem())) continue;

            boolean isRaining = BurnTimeUtils.isActuallyRainingAt(world, itemEntity.getBlockPos());
            boolean isSubmerged = itemEntity.isSubmergedIn(FluidTags.WATER);
            double rainMult = BurnableRegistry.getRainMultiplier(stack.getItem());
            double waterMult = BurnableRegistry.getWaterMultiplier(stack.getItem());

            if (isSubmerged && waterMult == 10.0) {
                burnTime = 0;
                LOGGER.debug("Instantly extinguished dropped item due to water submersion (multiplier=10)");
            } else {
                double effectiveMultiplier = 1.0;
                if (isRaining) {
                    effectiveMultiplier = Math.max(effectiveMultiplier, rainMult);
                }
                if (isSubmerged && waterMult > 0.0) {
                    effectiveMultiplier = Math.max(effectiveMultiplier, waterMult);
                }
                // Fire event to allow mods to modify decrement
                long elapsedTicks = ITEM_UPDATE_INTERVAL;
                long baseDecrement = (long) Math.ceil(elapsedTicks * effectiveMultiplier);
                BurnTickEvents.DroppedItemContext itemContext = new BurnTickEvents.DroppedItemContext(itemEntity, stack, baseDecrement);
                long finalDecrement = BurnTickEvents.DROPPED_ITEM.invoker().onTick(itemContext, baseDecrement);
                burnTime -= finalDecrement;
            }

            BurnTimeUtils.setCurrentBurnTime(stack, Math.max(0, burnTime));
            itemEntity.setStack(stack);

            if (burnTime <= 0) {
                ItemStack unlit = new ItemStack(Objects.requireNonNull(BurnableRegistry.getUnlitItem(stack.getItem())), stack.getCount());
                itemEntity.setStack(unlit);
            }
        }
    }

    private static void tickBurnableBlock(World world, BlockPos pos, BlockState state, Block block, Burnable burnable) {
        long burnTime = burnable.getRemainingBurnTime();
        if (burnTime <= 0) {
            Block unlit = BurnableRegistry.getUnlitBlock(state.getBlock());
            if (unlit != null && unlit != state.getBlock()) {
                BlockState newState = copyProperties(state, unlit.getDefaultState());
                world.setBlockState(pos, newState, 3);
                LOGGER.debug("Extinguished {} at {}", unlit, pos);
            }
            return;
        }
        if (!BurnableRegistry.isTickingEnabled(block)) return;

        // Fire event to allow mods to modify decrement
        double mult = burnable.getRainMultiplier() * (BurnTimeUtils.isActuallyRainingAt(world, pos) ? 1.0 : 0.0)
                + burnable.getWaterMultiplier() * (world.getFluidState(pos).isIn(FluidTags.WATER) ? 1.0 : 0.0);
        if (mult <= 0.0) mult = 1.0;
        long baseDecrement = (long) Math.ceil(ITEM_UPDATE_INTERVAL * mult);
        BurnTickEvents.BlockContext blockContext = new BurnTickEvents.BlockContext(world, pos, baseDecrement);
        long finalDecrement = BurnTickEvents.BLOCK.invoker().onTick(blockContext, baseDecrement);
        burnable.setRemainingBurnTime(burnTime - finalDecrement);
    }
    private static void tickCampfire(World world, BlockPos pos, BlockState state, ICampfireBurnAccessor campfire) {
        long burnTime = campfire.torchesbt_getBurnTime();

        if (burnTime > 0) {
            if (!BurnableRegistry.isTickingEnabled(Blocks.CAMPFIRE)) return;
            boolean isRaining = BurnTimeUtils.isActuallyRainingAt(world, pos);
            boolean isSubmerged = world.getFluidState(pos).isIn(FluidTags.WATER);
            double rainMult = BurnableRegistry.getRainMultiplier(Blocks.CAMPFIRE);
            double waterMult = BurnableRegistry.getWaterMultiplier(Blocks.CAMPFIRE);

            if (isSubmerged && waterMult == 10.0) {
                burnTime = 0;
                LOGGER.debug("Instantly extinguished campfire at {} due to water submersion (multiplier=10)", pos);
            } else {
                double effectiveMultiplier = 1.0;
                if (isRaining) {
                    effectiveMultiplier = Math.max(effectiveMultiplier, rainMult);
                }
                if (isSubmerged && waterMult > 0.0) {
                    effectiveMultiplier = Math.max(effectiveMultiplier, waterMult);
                }
                // Fire event to allow mods to modify decrement
                long baseDecrement = (long) Math.ceil(ITEM_UPDATE_INTERVAL * effectiveMultiplier);
                BurnTickEvents.BlockContext blockContext = new BurnTickEvents.BlockContext(world, pos, baseDecrement);
                long finalDecrement = BurnTickEvents.BLOCK.invoker().onTick(blockContext, baseDecrement);
                burnTime -= finalDecrement;
            }
            campfire.torchesbt_setBurnTime(Math.max(0, burnTime));
        }

        if (burnTime <= 0 && state.get(CampfireBlock.LIT)) {
            world.setBlockState(pos, state.with(CampfireBlock.LIT, false), 3);
        }

// Sync to client
        if (!world.isClient) {
            world.updateListeners(pos, state, state, 3);
        }
    }

    public static void setBurnTimeOnPlacement(World world, BlockPos pos, BlockEntity entity, ItemStack stack, long defaultBurnTime) {
        long burnTime = BurnTimeUtils.getCurrentBurnTime(stack);
        if (burnTime <= 0) burnTime = defaultBurnTime;

        if (entity instanceof Burnable burnable) {
            burnable.setRemainingBurnTime(burnTime);
        } else if (entity instanceof ICampfireBurnAccessor campfire) {
            campfire.torchesbt_setBurnTime(burnTime);
        }
    }
}