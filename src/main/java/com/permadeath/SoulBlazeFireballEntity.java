package com.permadeath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;

/** Proyectil del Blaze Fatuo: como la bola de fuego del Blaze, pero quema mas fuerte y mas tiempo. */
public class SoulBlazeFireballEntity extends SmallFireballEntity {
    public SoulBlazeFireballEntity(EntityType<? extends SmallFireballEntity> type, World world) {
        super(type, world);
    }

    public SoulBlazeFireballEntity(World world, LivingEntity owner, double dx, double dy, double dz) {
        super(world, owner, dx, dy, dz);
    }

    @Override
    protected void onEntityHit(EntityHitResult hitResult) {
        super.onEntityHit(hitResult);
        if (this.getWorld().isClient) {
            return;
        }
        Entity target = hitResult.getEntity();
        if (!target.isFireImmune()) {
            // quemadura mas intensa y mas larga que el fuego normal del Blaze
            target.setFireTicks(target.getFireTicks() + 160);
            if (this.getOwner() instanceof LivingEntity owner) {
                target.damage(this.getDamageSources().mobAttack(owner), 2.0f);
            }
        }
    }
}
