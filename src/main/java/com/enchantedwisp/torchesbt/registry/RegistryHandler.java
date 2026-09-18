package com.enchantedwisp.torchesbt.registry;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import com.enchantedwisp.torchesbt.core.BurnableRegistry;
import com.enchantedwisp.torchesbt.core.DefaultFuelTypes;
import com.enchantedwisp.torchesbt.registry.blocks.UnlitLanternBlock;
import com.enchantedwisp.torchesbt.registry.blocks.UnlitTorchBlock;
import com.enchantedwisp.torchesbt.registry.blocks.UnlitWallTorchBlock;
import com.enchantedwisp.torchesbt.registry.items.SparkStoneItem;
import com.enchantedwisp.torchesbt.registry.items.UnlitCampFireItem;
import com.enchantedwisp.torchesbt.registry.items.UnlitLanternItem;
import com.enchantedwisp.torchesbt.registry.items.UnlitTorchItem;
import com.enchantedwisp.torchesbt.util.ConfigCache;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import static com.enchantedwisp.torchesbt.RealisticTorchesBT.MOD_ID;
import static com.enchantedwisp.torchesbt.RealisticTorchesBT.getConfig;

public class RegistryHandler {
    private static final Logger LOGGER = RealisticTorchesBT.LOGGER;

    public static final Block UNLIT_TORCH_BLOCK = new UnlitTorchBlock();
    public static final Block UNLIT_WALL_TORCH_BLOCK = new UnlitWallTorchBlock();
    public static final Block UNLIT_LANTERN_BLOCK = new UnlitLanternBlock();
    public static final Item UNLIT_TORCH = new UnlitTorchItem((UnlitTorchBlock) UNLIT_TORCH_BLOCK, new Item.Settings());
    public static final Item UNLIT_LANTERN = new UnlitLanternItem(UNLIT_LANTERN_BLOCK, new Item.Settings());
    public static final Item UNLIT_CAMPFIRE = new UnlitCampFireItem(new Item.Settings());
    public static final Item SPARK_STONE = new SparkStoneItem(new Item.Settings().maxCount(1).maxDamage(20));

    public static void register() {
        registerItemGroups();
        registerUnlitGroups();
        RealisticTorchesBT.LOGGER.info("Registering blocks, items, and block entities for Realistic torches BT");

        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "unlit_torch"), UNLIT_TORCH_BLOCK);
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "unlit_wall_torch"), UNLIT_WALL_TORCH_BLOCK);
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "unlit_lantern"), UNLIT_LANTERN_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "unlit_torch"), UNLIT_TORCH);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "unlit_lantern"), UNLIT_LANTERN);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "unlit_campfire"), UNLIT_CAMPFIRE);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "spark_stone"), SPARK_STONE);
    }

    private static void registerUnlitGroups() {
        ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(UNLIT_TORCH);
            entries.add(UNLIT_LANTERN);
            entries.add(UNLIT_CAMPFIRE);
        });
    }

    private static void registerItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(SPARK_STONE));
    }

    public static void registerBurnables() {

        BurnableRegistry.registerBurnableItem(
                Items.TORCH,
                RegistryHandler.UNLIT_TORCH,
                ConfigCache.getTorchBurnTime(),
                ConfigCache.getRainTorchMultiplier(),
                ConfigCache.getWaterTorchMultiplier(),
                ConfigCache.isAllowTorchTick()
        );
        BurnableRegistry.registerBurnableItem(
                Items.LANTERN,
                RegistryHandler.UNLIT_LANTERN,
                ConfigCache.getLanternBurnTime(),
                ConfigCache.getRainLanternMultiplier(),
                ConfigCache.getWaterLanternMultiplier(),
                ConfigCache.isAllowLanternTick()
        );
        BurnableRegistry.registerBurnableBlock(
                Blocks.TORCH,
                RegistryHandler.UNLIT_TORCH_BLOCK,
                ConfigCache.getTorchBurnTime(),
                ConfigCache.getRainTorchMultiplier(),
                ConfigCache.getWaterTorchMultiplier(),
                true,
                DefaultFuelTypes.TORCH,
                ConfigCache.isAllowTorchTick()
        );
        BurnableRegistry.registerBurnableBlock(
                Blocks.WALL_TORCH,
                RegistryHandler.UNLIT_WALL_TORCH_BLOCK,
                ConfigCache.getTorchBurnTime(),
                ConfigCache.getRainTorchMultiplier(),
                ConfigCache.getWaterTorchMultiplier(),
                true,
                DefaultFuelTypes.TORCH,
                ConfigCache.isAllowTorchTick()
        );
        BurnableRegistry.registerBurnableBlock(
                Blocks.LANTERN,
                RegistryHandler.UNLIT_LANTERN_BLOCK,
                ConfigCache.getLanternBurnTime(),
                ConfigCache.getRainLanternMultiplier(),
                ConfigCache.getWaterLanternMultiplier(),
                true,
                DefaultFuelTypes.LANTERN,
                ConfigCache.isAllowLanternTick()
        );
        BurnableRegistry.registerBurnableBlock(
                Blocks.CAMPFIRE,
                Blocks.CAMPFIRE, // Unlit uses same block with LIT=false
                ConfigCache.getCampfireBurnTime(),
                ConfigCache.getRainCampfireMultiplier(),
                ConfigCache.getWaterCampfireMultiplier(),
                true,
                DefaultFuelTypes.CAMPFIRE,
                ConfigCache.isAllowCampfireTick()
        );
        BurnableRegistry.snapshotCounts("Vanilla");
        BurnableRegistry.logSource("Vanilla", LOGGER);
    }
}