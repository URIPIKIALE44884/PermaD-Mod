package com.permadeath;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.RangedAttackMob;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Blaze Fatuo: version "fuego de almas" del Blaze. El doble de vida y mas dano fisico que un Blaze
 * normal, y dispara proyectiles de fuego de almas (mas dano y quemadura mas larga). Se consigue
 * transformando permanentemente los generadores de la Fortaleza del Wildfire al entrar en su 2da fase.
 */
public class BlazeFatuoEntity extends BlazeEntity implements RangedAttackMob {
    public BlazeFatuoEntity(EntityType<? extends BlazeEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(4, new ProjectileAttackGoal(this, 1.0, 20, 60, 48.0f));
    }

    public static DefaultAttributeContainer.Builder createBlazeFatuoAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)   // el doble de los 20 del Blaze
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0) // Blaze normal: 6
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    @Override
    public void attack(net.minecraft.entity.LivingEntity target, float pullProgress) {
        // reemplaza la bola de fuego normal del Blaze por una de fuego de almas
        double dx = target.getX() - this.getX();
        double dy = target.getBodyY(0.5) - this.getBodyY(0.5);
        double dz = target.getZ() - this.getZ();
        SoulBlazeFireballEntity fireball = new SoulBlazeFireballEntity(this.getWorld(), this,
                dx + this.random.nextGaussian() * 0.4, dy, dz + this.random.nextGaussian() * 0.4);
        fireball.refreshPositionAndAngles(this.getX(), this.getBodyY(0.5), this.getZ(), 0.0f, 0.0f);
        this.getWorld().spawnEntity(fireball);
        this.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 2.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BLAZE_DEATH;
    }
}
