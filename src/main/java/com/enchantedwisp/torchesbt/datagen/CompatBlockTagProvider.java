package com.enchantedwisp.torchesbt.datagen;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.Surrogate;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class CompatBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    private static final String[] CHIPPED_LANTERNS = {
            "blue_paper", "burning_coal", "checkered_iron", "dark_blue_paper", "ender",
            "green_paper", "iron_bowl", "purple_paper", "red_paper", "small_green",
            "white_paper", "wooden_cage", "wrought_iron", "yellow_tube"
    };
    private static final String[] SPECIAL_LANTERNS = {
            "big", "donut", "tall", "wide"
    };

    @Override
    public String getName() {
        return "Realistic Torches BT/Compat Tags for minecraft:block";
    }

    public CompatBlockTagProvider(FabricDataOutput output,
                                  CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(
                new FabricDataOutput(
                        output.getModContainer(),
                        output.getPath().resolve("resourcepacks").resolve("chipped_compat"),
                        output.isStrictValidationEnabled()
                ),
                registriesFuture
        );
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        for (String variant : CHIPPED_LANTERNS) {
            Block unlitBlock = Registries.BLOCK.get(
                    Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_lantern")
            );
            getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(unlitBlock);
        }

        for (String variant : SPECIAL_LANTERNS) {
            Block unlitBlock = Registries.BLOCK.get(
                    Identifier.of(RealisticTorchesBT.MOD_ID, "chipped/unlit_" + variant + "_lantern")
            );
            getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(unlitBlock);
        }
    }
}

