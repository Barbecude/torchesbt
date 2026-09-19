package com.enchantedwisp.torchesbt.core.ignition;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.api.IgnitionEvents;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeManager;
import com.enchantedwisp.torchesbt.core.burn.BurnTimeUtils;
import com.enchantedwisp.torchesbt.mixinaccess.ICampfireBurnAccessor;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.util.JsonLoader;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;


/**
 * Handles ignition of unlit torches, lanterns, and campfires using defined igniter items.
 * Fires IgnitionEvents to allow external mods to cancel or modify ignition burn times.
 *
 * <p>Igniters are defined in {@link JsonLoader#IGNITERS}.</p>
 */
public class IgnitionHandler {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    public static boolean isIgniter(ItemStack stack, Identifier itemId) {
        if (stack.isEmpty()) return false;
        if (JsonLoader.IGNITERS.containsKey(itemId)) return true;
        return stack.isOf(net.minecraft.item.Items.FLINT_AND_STEEL) || stack.isOf(net.minecraft.item.Items.FIRE_CHARGE);
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            ItemStack offHandStack = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            Identifier offHandItemId = Registries.ITEM.getId(offHandStack.getItem());

            if (isIgniter(offHandStack, offHandItemId)) {
                Item litItem = BurnableRegistry.getLitItem(stack.getItem());
                if (litItem != null) {
                    if (world.isClient) {
                        return TypedActionResult.success(stack);
                    }
                    int igniteAmount = JsonLoader.IGNITERS.getOrDefault(offHandItemId, 100);
                    long proposed = igniteAmount * 20L;
                    long maxBurn = BurnableRegistry.getBurnTime(litItem);
                    if (maxBurn > 0) proposed = Math.max(proposed, maxBurn);

                    IgnitionEvents.ItemContext itemContext = new IgnitionEvents.ItemContext(player, hand, stack, offHandStack, proposed);
                    ActionResult itemResult = IgnitionEvents.ITEM.invoker().onIgnite(itemContext);
                    if (itemResult == ActionResult.FAIL) {
                        return TypedActionResult.fail(stack);
                    }
                    return igniteItem(world, hand, stack, offHandStack, litItem, stack.getItem(), player, itemContext.getProposedBurnTime());
                }
            }

            if (isIgniter(stack, itemId)) {
                Item litItem = BurnableRegistry.getLitItem(offHandStack.getItem());
                if (litItem != null) {
                    if (world.isClient) {
                        return TypedActionResult.success(stack);
                    }
                    int igniteAmount = JsonLoader.IGNITERS.getOrDefault(itemId, 100);
                    long proposed = igniteAmount * 20L;
                    long maxBurn = BurnableRegistry.getBurnTime(litItem);
                    if (maxBurn > 0) proposed = Math.max(proposed, maxBurn);

                    IgnitionEvents.ItemContext itemContext = new IgnitionEvents.ItemContext(player, hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, offHandStack, stack, proposed);
                    ActionResult itemResult = IgnitionEvents.ITEM.invoker().onIgnite(itemContext);
                    if (itemResult == ActionResult.FAIL) {
                        return TypedActionResult.fail(stack);
                    }
                    return igniteItem(world, hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, offHandStack, stack, litItem, offHandStack.getItem(), player, itemContext.getProposedBurnTime());
                }
            }

            return TypedActionResult.pass(stack);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            if (!isIgniter(stack, itemId)) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block litBlock = BurnableRegistry.getLitBlock(state.getBlock());

            boolean isCampfire = (litBlock == Blocks.CAMPFIRE || state.isOf(Blocks.CAMPFIRE));
            boolean canIgniteCampfire = isCampfire && state.contains(CampfireBlock.LIT) && !state.get(CampfireBlock.LIT);
            boolean canIgniteOther = (litBlock != null && litBlock != state.getBlock());

            if (!canIgniteCampfire && !canIgniteOther) {
                return ActionResult.PASS;
            }

            // CRITICAL: Return SUCCESS on client to consume the interaction, swing arm, send packet to server,
            // and prevent vanilla FlintAndSteelItem from attempting fire placement or returning FAIL!
            if (world.isClient) {
                return ActionResult.SUCCESS;
            }

            int igniteAmount = JsonLoader.IGNITERS.getOrDefault(itemId, 100);
            long defaultBurn = igniteAmount * 20L;
            Block actualLitBlock = isCampfire ? Blocks.CAMPFIRE : litBlock;
            long maxBurn = BurnableRegistry.getBurnTime(actualLitBlock);
            if (maxBurn <= 0 && isCampfire) {
                maxBurn = com.enchantedwisp.torchesbt.util.ConfigCache.getCampfireBurnTime();
            }

            IgnitionEvents.BlockContext blockContext = new IgnitionEvents.BlockContext(player, hand, world, pos, stack, defaultBurn);
            ActionResult blockResult = IgnitionEvents.BLOCK.invoker().onIgnite(blockContext);
            if (blockResult == ActionResult.FAIL) {
                return ActionResult.FAIL;
            }
            long finalBurnTime = blockContext.getProposedBurnTime();
            if (finalBurnTime <= 0) {
                finalBurnTime = maxBurn > 0 ? maxBurn : defaultBurn;
            } else if (maxBurn > 0) {
                finalBurnTime = Math.max(finalBurnTime, maxBurn);
            }

            if (canIgniteCampfire) {
                world.setBlockState(pos, state.with(CampfireBlock.LIT, true), 3);
                ICampfireBurnAccessor accessor = (ICampfireBurnAccessor) world.getBlockEntity(pos);
                if (accessor != null) {
                    accessor.torchesbt_setBurnTime(finalBurnTime);
                    LOGGER.debug("Ignited campfire at {} with burn time {}", pos, finalBurnTime);
                }
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                ReignitionHandler.consumeIgniter(stack, player, hand);
                return ActionResult.SUCCESS;
            } else if (canIgniteOther) {
                BlockState newState = copyProperties(state, litBlock.getDefaultState());
                world.setBlockState(pos, newState, 3);
                BurnTimeManager.setBurnTimeOnPlacement(world, pos, world.getBlockEntity(pos),
                        stack, finalBurnTime);
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                ReignitionHandler.consumeIgniter(stack, player, hand);
                LOGGER.debug("Ignited block {} at {} using {}", litBlock, pos, stack.getItem());
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    private static TypedActionResult<ItemStack> igniteItem(World world, Hand hand, ItemStack stack, ItemStack igniter,
                                                           Item litItem, Item unlitItem, PlayerEntity player, long finalBurnTime) {
        if (world.isClient) return TypedActionResult.pass(stack);

        int stackCount = stack.getCount();
        ItemStack newStack = new ItemStack(litItem, 1);
        BurnTimeUtils.setCurrentBurnTime(newStack, finalBurnTime);  // Use event-modified burn time
        player.setStackInHand(hand, newStack);
        ReignitionHandler.consumeIgniter(igniter, player, hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        player.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F);

        // Handle remaining unlit items
        if (stackCount > 1) {
            ItemStack remaining = new ItemStack(unlitItem, stackCount - 1);
            if (!player.getInventory().insertStack(remaining)) {
                world.spawnEntity(new net.minecraft.entity.ItemEntity(world, player.getX(), player.getY(), player.getZ(), remaining));
            }
        }
        return TypedActionResult.success(newStack);
    }

    // --- Helper to copy orientation/waterlogging/etc. ---
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BlockState copyProperties(BlockState from, BlockState to) {
        for (Property<?> property : from.getProperties()) {
            if (to.contains(property)) {
                to = to.with((Property) property, from.get(property));
            }
        }
        return to;
    }
}