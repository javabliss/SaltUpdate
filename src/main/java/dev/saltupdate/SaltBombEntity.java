package dev.saltupdate;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

/** Брошенная соляная бомба: при попадании слепит всех живых существ рядом на 5 секунд. */
public class SaltBombEntity extends ThrowableItemProjectile {
    private static final double RADIUS = 3.5;

    public SaltBombEntity(EntityType<? extends SaltBombEntity> type, Level level) {
        super(type, level);
    }

    public SaltBombEntity(Level level, LivingEntity shooter) {
        super(SaltUpdate.SALT_BOMB.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return SaltUpdate.SALT_BOMB_ITEM.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        ServerLevel server = (ServerLevel) level();
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(RADIUS))) {
            if (target == getOwner() || target.distanceToSqr(this) > RADIUS * RADIUS) continue;
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), getEffectSource());
        }
        server.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 50, 1.2, 0.6, 1.2, 0.03);
        server.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.0F, 0.8F);
        discard();
    }
}
