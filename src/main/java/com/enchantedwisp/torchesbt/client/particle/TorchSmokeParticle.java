package com.enchantedwisp.torchesbt.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class TorchSmokeParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    protected TorchSmokeParticle(ClientWorld world, double x, double y, double z,
                                 double vx, double vy, double vz, SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;

        // Random size
        this.scale = 0.08f + this.random.nextFloat() * 0.05f;
        this.maxAge = 40 + this.random.nextInt(20);
        this.gravityStrength = 0.0f;

        // Give upward drift
        this.velocityX *= 0.02;
        this.velocityY = 0.02 + this.random.nextFloat() * 0.02;
        this.velocityZ *= 0.02;

        // Random grayscale
        float grayValue = 0.2F + this.random.nextFloat() * 0.3F;
        this.setColor(grayValue, grayValue, grayValue);

        this.alpha = 0.9f;

        // 🔑 Randomize starting age (so not all particles start at frame 0)
        this.age = this.random.nextInt(this.maxAge / 2);

        // 🔑 Make sure we assign an initial sprite immediately
        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out over time
        this.alpha = ((float) this.maxAge - this.age) / (float) this.maxAge;

        // Shrink slightly
        this.scale *= 0.98f;

        // 🔑 Update sprite linearly by age
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
            return new TorchSmokeParticle(world, x, y, z, vx, vy, vz, spriteProvider);
        }
    }
}
