package com.enchantedwisp.torchesbt.core;

import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import net.minecraft.util.Identifier;

public class DefaultFuelTypes {
    public static final FuelTypeAPI.FuelType TORCH =
            FuelTypeAPI.registerFuelType(Identifier.of("torchesbt", "torch"));

    public static final FuelTypeAPI.FuelType LANTERN =
            FuelTypeAPI.registerFuelType(Identifier.of("torchesbt", "lantern"));

    public static final FuelTypeAPI.FuelType CAMPFIRE =
            FuelTypeAPI.registerFuelType(Identifier.of("torchesbt", "campfire"));

    private DefaultFuelTypes() {
    }
}
