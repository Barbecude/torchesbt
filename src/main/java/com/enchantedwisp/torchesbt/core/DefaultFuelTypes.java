package com.enchantedwisp.torchesbt.core;

import com.enchantedwisp.torchesbt.api.FuelTypeAPI;
import net.minecraft.util.Identifier;

public class DefaultFuelTypes {
    public static final FuelTypeAPI.FuelType TORCH =
            FuelTypeAPI.registerFuelType(new Identifier("torchesbt", "torch"));

    public static final FuelTypeAPI.FuelType LANTERN =
            FuelTypeAPI.registerFuelType(new Identifier("torchesbt", "lantern"));

    public static final FuelTypeAPI.FuelType CAMPFIRE =
            FuelTypeAPI.registerFuelType(new Identifier("torchesbt", "campfire"));

    private DefaultFuelTypes() {
    }
}
