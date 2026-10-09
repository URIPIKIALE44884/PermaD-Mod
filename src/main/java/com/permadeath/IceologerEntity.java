package com.permadeath;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Iceologer (adaptado del mod NeoForge "Iceologer Mod", reescrito para Fabric).
 * Tiene 10 de vida, velocidad 0,3, dano 8 y rango de seguimiento 16.
 * Los drops especiales son probabilisticos para que no esten garantizados.
 */
public class IceologerEntity extends IllagerEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    private int iceChunkCooldown = 0;

    public IceologerEntity(EntityType<? extends IceologerEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(8, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0f, 1.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0f));
        this.goalSelector.add(11, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, RaiderEntity.class).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createIceologerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    public IllagerEntity.State getState() {
        if (this.isAttacking()) {
            return IllagerEntity.State.ATTACKING;
        }
        if (this.isCelebrating()) {
            return IllagerEntity.State.CELEBRATING;
        }
        return IllagerEntity.State.CROSSED;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            this.idleAnimationState.startIfNotRunning(this.age);
        }
        if (this.iceChunkCooldown > 0) {
            this.iceChunkCooldown--;
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        super.onPlayerCollision(player);
        World world = this.getWorld();
        if (world.isClient || this.iceChunkCooldown > 0) {
            return;
        }
        if (!world.getGameRules().getBoolean(ModGameRules.DROP_ICE_CHUNKS)) {
            return;
        }
        this.iceChunkCooldown = 60;
        BlockPos base = BlockPos.ofFloored(this.getX(), this.getY() + 2.0, this.getZ());
        BlockPos[] spots = { base, base.east(), base.south(), base.east().south() };
        for (BlockPos pos : spots) {
            if (!world.isAir(pos)) {
                continue;
            }
            FallingBlockEntity chunk = FallingBlockEntity.spawnFromBlock(world, pos, Blocks.PACKED_ICE.getDefaultState());
            chunk.setHurtEntities(2.0f, 20);
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        World world = this.getWorld();
        if (!world.isClient && world.getGameRules().getBoolean(ModGameRules.ICEOLOGER_ICE_ON_FALL)) {
            BlockPos below = this.getBlockPos().down();
            BlockState state = world.getBlockState(below);
            if (!state.isAir() && state.getHardness(world, below) >= 0) {
                world.setBlockState(below, Blocks.PACKED_ICE.getDefaultState(), 3);
            }
        }
        return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
    }

    /** Hielo compacto: 50%; libro Ice Aspect I: 20%. */
    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        if (this.random.nextFloat() < 0.50f) {
            this.dropStack(new ItemStack(Blocks.PACKED_ICE));
        }
        if (this.random.nextFloat() < 0.20f) {
            this.dropStack(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(ModEnchantments.ICE_ASPECT, 1)));
        }
    }

    @Override
    public void addBonusForWave(int wave, boolean unused) {
        // sin bonus: el Iceologer no lleva equipo
    }

    @Override
    public SoundEvent getCelebratingSound() {
        return SoundEvents.ENTITY_PILLAGER_CELEBRATE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PILLAGER_DEATH;
    }
}
