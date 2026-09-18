package com.enchantedwisp.torchesbt.core;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;

public enum FlameLevel implements StringIdentifiable {
    LOW("low"),
    MID("mid"),
    FULL("full");

    private final String name;

    FlameLevel(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    // Expose the property so other classes can use it
    public static final EnumProperty<FlameLevel> PROPERTY =
            EnumProperty.of("flame_level", FlameLevel.class);
}