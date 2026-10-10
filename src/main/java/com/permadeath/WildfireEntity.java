package com.permadeath;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;

/**
 * Jefe de la Fortaleza del Wildfire. Diseno propio (reescrito, no copiado del addon original), pensado
 * para Fabric 1.20.6: vuela, dispara bolas de fuego, tiene barra de jefe y una 2da fase permanente:
 * al bajar al 30% de vida transforma para siempre los generadores de su fortaleza en Blaze Fatuo.
 */
public class WildfireEntity extends HostileEntity implements RangedAttackMob {
    private static final TrackedData<Boolean> PHASE2 =
            DataTracker.registerData(WildfireEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    private final List<BlockPos> spawnerPositions = new ArrayList<>();

    public WildfireEntity(EntityType<? extends WildfireEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 50;
        this.moveControl = new FlightMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(PHASE2, false);
    }

    public boolean isPhase2() {
        return this.dataTracker.get(PHASE2);
    }

    /** Llamado por la estructura justo despues de aparecer: guarda donde estan sus 4 generadores de Blaze. */
    public void setSpawnerPositions(List<BlockPos> positions) {
        this.spawnerPositions.clear();
        this.spawnerPositions.addAll(positions);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanPathThroughDoors(false);
        navigation.setCanSwim(true);
        navigation.setCanEnterOpenDoors(true);
        return navigation;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(4, new ProjectileAttackGoal(this, 0.7, 40, 20, 64.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createWildfireAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0);
    }

    @Override
    public void attack(net.minecraft.entity.LivingEntity target, float pullProgress) {
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
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
            if (this.age % 10 == 0) {
                this.setFireTicks(1); // el Wildfire siempre arde visualmente
            }
            if (!this.isPhase2() && this.getHealth() <= this.getMaxHealth() * 0.3f) {
                this.triggerPhase2();
            }
        } else if (this.age % 3 == 0) {
            this.getWorld().addParticle(this.isPhase2() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getWidth(),
                    this.getY() + this.random.nextDouble() * this.getHeight(),
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getWidth(), 0.0, 0.03, 0.0);
        }
    }

    /** Se activa una sola vez: transforma PARA SIEMPRE los generadores de esta fortaleza en Blaze Fatuo. */
    private void triggerPhase2() {
        this.dataTracker.set(PHASE2, true);
        this.playSound(SoundEvents.ENTITY_WITHER_AMBIENT, 2.0f, 0.6f);
        this.heal(this.getMaxHealth() * 0.1f);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            for (BlockPos pos : this.spawnerPositions) {
                BlockEntity entity = serverWorld.getBlockEntity(pos);
                if (entity instanceof MobSpawnerBlockEntity spawner) {
                    spawner.getLogic().setEntityId(ModEntities.BLAZE_FATUO, serverWorld, this.random, pos);
                    serverWorld.markDirty(pos);
                }
            }
        }
    }

    @Override
    public void onStartedTrackingBy(net.minecraft.server.network.ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(net.minecraft.server.network.ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    /** Botin: gema del Wildfire (probable, no garantizada) y varillas de Blaze al morir a manos de un jugador. */
    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        if (this.random.nextFloat() < 0.75f) {
            this.dropStack(new ItemStack(ModItems.WILDFIRE_GEM));
        }
        if (causedByPlayer) {
            this.dropStack(new ItemStack(Items.BLAZE_ROD, 5 + this.random.nextInt(6)));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Phase2", this.isPhase2());
        NbtList list = new NbtList();
        for (BlockPos pos : this.spawnerPositions) {
            NbtCompound p = new NbtCompound();
            p.putInt("X", pos.getX());
            p.putInt("Y", pos.getY());
            p.putInt("Z", pos.getZ());
            list.add(p);
        }
        nbt.put("SpawnerPositions", list);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(PHASE2, nbt.getBoolean("Phase2"));
        this.spawnerPositions.clear();
        for (int i = 0; i < nbt.getList("SpawnerPositions", 10).size(); i++) {
            NbtCompound p = nbt.getList("SpawnerPositions", 10).getCompound(i);
            this.spawnerPositions.add(new BlockPos(p.getInt("X"), p.getInt("Y"), p.getInt("Z")));
        }
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
        return SoundEvents.ENTITY_WITHER_DEATH;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }
}
