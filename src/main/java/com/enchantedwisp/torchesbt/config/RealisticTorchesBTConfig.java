package com.enchantedwisp.torchesbt.config;

import com.enchantedwisp.torchesbt.RealisticTorchesBT;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "torchesbt")
public class RealisticTorchesBTConfig implements ConfigData {
    @Comment("Enable dynamic lighting support (ignores whether a mod is installed). Default: false")
    @ConfigEntry.Gui.PrefixText
    public boolean enableDynamicLights = false;

    @Comment("Allow refueling items in slots. Default: true")
    @ConfigEntry.Gui.PrefixText
    public boolean allowInventoryRefueling = true;

    @Comment("Allow torches to tick, causing them to burn out over time. Default: true")
    @ConfigEntry.Gui.PrefixText
    public boolean allowTorchTick = true;

    @Comment("Allow lanterns to tick, causing them to burn out over time. Default: true")
    @ConfigEntry.Gui.PrefixText
    public boolean allowLanternTick = true;

    @Comment("Allow campfire to tick, causing them to burn out over time. Default: true")
    @ConfigEntry.Gui.PrefixText
    public boolean allowCampfireTick = true;

    @Comment("Burn time for torches. Default: 40 Max: 3600")
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.BoundedDiscrete(min = 5, max = 3600)
    public int torchBurnTime = 40;

    @Comment("Burn time for lanterns. Default: 60 Max: 3600")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 3600)
    public int lanternBurnTime = 60;

    @Comment("Burn time for campfires. Default: 100 Max: 3600")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 3600)
    public int campfireBurnTime = 100;

    @Comment("If true, rain affects Burn Time on burnables. Default: true")
    @ConfigEntry.Gui.PrefixText
    public boolean enableRainExtinguish = true;

    @Comment("Burn time multiplier for torches in rain. Default: 10.0 (10x faster) Max: 10x")
    public double rainTorchMultiplier = 10;

    @Comment("Burn time multiplier for campfires in rain. Default: 8.5 (8.5x faster) Max: 10x")
    public double rainCampfireMultiplier = 8.5;

    @Comment("Burn time multiplier for lanterns in rain. Default: 4.5 (4.5x faster) Max: 10x")
    public double rainLanternMultiplier = 4.5;

    @Comment("Burn time multiplier for torches in water. Default: 7.5 (7.5x faster) Max: 10x instant extinguish")
    public double waterTorchMultiplier = 10;

    @Comment("Burn time multiplier for lanterns in water. Default: 7.5 (7.5x faster) Max: 10x instant extinguish")
    public double waterLanternMultiplier = 7.5;

    @Comment("Burn time multiplier for campfires in water. Default: 7.5 (7.5x faster) Max: 10x instant extinguish")
    public double waterCampfireMultiplier = 10;

    @Override
    public void validatePostLoad() {
        // Clamp BurnTime
        if (lanternBurnTime < 5 || lanternBurnTime > 3600) {
            RealisticTorchesBT.LOGGER.warn("Correcting torchBurnTime: {} to {}. Must be between 5 and 3600", lanternBurnTime, Math.max(5, Math.min(3600, lanternBurnTime)));
            lanternBurnTime = Math.max(5, Math.min(3600, lanternBurnTime));
        }
        if (campfireBurnTime < 5 || campfireBurnTime > 3600) {
            RealisticTorchesBT.LOGGER.warn("Correcting torchBurnTime: {} to {}. Must be between 5 and 3600", campfireBurnTime, Math.max(5, Math.min(3600, campfireBurnTime)));
            campfireBurnTime = Math.max(5, Math.min(3600, campfireBurnTime));
        }
        // Clamp multipliers
        if (rainTorchMultiplier < 1.0 || rainTorchMultiplier > 10.0) {
            RealisticTorchesBT.LOGGER.warn("Correcting rainTorchMultiplier: {} to {}. Must be between 1.0 and 10.0.", rainTorchMultiplier, Math.max(1.0, Math.min(10.0, rainTorchMultiplier)));
            rainTorchMultiplier = Math.max(1.0, Math.min(10.0, rainTorchMultiplier));
        }
        if (rainCampfireMultiplier < 1.0 || rainCampfireMultiplier > 10.0) {
            RealisticTorchesBT.LOGGER.warn("Correcting rainCampfireMultiplier: {} to {}. Must be between 1.0 and 10.0.", rainCampfireMultiplier, Math.max(1.0, Math.min(10.0, rainCampfireMultiplier)));
            rainCampfireMultiplier = Math.max(1.0, Math.min(10.0, rainCampfireMultiplier));
        }
        if (rainLanternMultiplier < 1.0 || rainLanternMultiplier > 10.0) {
            RealisticTorchesBT.LOGGER.warn("Correcting rainLanternMultiplier: {} to {}. Must be between 1.0 and 10.0.", rainLanternMultiplier, Math.max(1.0, Math.min(10.0, rainLanternMultiplier)));
            rainLanternMultiplier = Math.max(1.0, Math.min(10.0, rainLanternMultiplier));
        }
        if (waterTorchMultiplier < 1.0 || waterTorchMultiplier > 10.0) {
            RealisticTorchesBT.LOGGER.warn("Correcting waterTorchMultiplier: {} to {}. Must be between 1.0 and 10.0.", waterTorchMultiplier, Math.max(1.0, Math.min(10.0, waterTorchMultiplier)));
            waterTorchMultiplier = Math.max(1.0, Math.min(10.0, waterTorchMultiplier));
        }
        if (waterLanternMultiplier < 1.0 || waterLanternMultiplier > 10.0) {
            RealisticTorchesBT.LOGGER.warn("Correcting waterLanternMultiplier: {} to {}. Must be between 1.0 and 10.0.", waterLanternMultiplier, Math.max(1.0, Math.min(10.0, waterLanternMultiplier)));
            waterLanternMultiplier = Math.max(1.0, Math.min(10.0, waterLanternMultiplier));
        }
        if (waterCampfireMultiplier < 1.0 || waterCampfireMultiplier > 10.0) {
            RealisticTorchesBT.LOGGER.warn("Correcting waterCampfireMultiplier: {} to {}. Must be between 1.0 and 10.0.", waterCampfireMultiplier, Math.max(1.0, Math.min(10.0, waterCampfireMultiplier)));
            waterCampfireMultiplier = Math.max(1.0, Math.min(10.0, waterCampfireMultiplier));
        }
    }
}