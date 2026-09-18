package com.enchantedwisp.torchesbt.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class CandleSmokeParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    protected CandleSmokeParticle(ClientWorld world, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;

        // Candle smoke is very thin & small
        this.scale = 0.03f + this.random.nextFloat() * 0.01f;

        // Longer lifespan so it drifts higher
        this.maxAge = 80 + this.random.nextInt(40);

        this.gravityStrength = 0.0f;

        // Gentle motion
        this.velocityX *= 0.01;
        this.velocityY = 0.005 + this.random.nextFloat() * 0.01; // much slower upward start
        this.velocityZ *= 0.01;

        // Random grayscale, slightly lighter for candle smoke
        float grayValue = 0.3F + this.random.nextFloat() * 0.3F;
        this.setColor(grayValue, grayValue, grayValue);

        this.alpha = 0.9f;

        this.age = this.random.nextInt(this.maxAge / 2);
        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out smoothly
        this.alpha = ((float) this.maxAge - this.age) / (float) this.maxAge;

        // Slowly shrink
        this.scale *= 0.99f;

        // Add a tiny upward acceleration so it drifts higher
        this.velocityY += 0.001;

        // Keep sprite animation in sync
        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new CandleSmokeParticle(world, x, y, z, vx, vy, vz, spriteProvider);
        }
    }
}
