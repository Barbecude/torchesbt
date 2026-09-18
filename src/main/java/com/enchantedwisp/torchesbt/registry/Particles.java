package com.enchantedwisp.torchesbt.registry;

import com.enchantedwisp.torchesbt.client.particle.CandleSmokeParticle;
import com.enchantedwisp.torchesbt.client.particle.TorchSmokeParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.enchantedwisp.torchesbt.RealisticTorchesBT.MOD_ID;

public class Particles {
    public static final DefaultParticleType CANDLE_SMOKE = FabricParticleTypes.simple(false);
    public static final DefaultParticleType TORCH_SMOKE = FabricParticleTypes.simple(false);

    public static void register() {
        Registry.register(
                Registries.PARTICLE_TYPE,
                new Identifier(MOD_ID, "candle_smoke"),
                CANDLE_SMOKE
        );

        Registry.register(
                Registries.PARTICLE_TYPE,
                new Identifier(MOD_ID, "torch_smoke"),
                TORCH_SMOKE
        );
    }

    public static void registerClient() {
        ParticleFactoryRegistry.getInstance().register(CANDLE_SMOKE, CandleSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(TORCH_SMOKE, TorchSmokeParticle.Factory::new);
    }
}
