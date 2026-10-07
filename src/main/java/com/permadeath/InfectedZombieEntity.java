package com.permadeath;

import java.util.UUID;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Zombie que aparece donde muere un jugador infectado: usa la skin del jugador,
 * nombre en rojo con un icono de zombie, +2 filas de corazones, Fuerza I y Resistencia al fuego.
 */
public class InfectedZombieEntity extends ZombieEntity {
    private static final TrackedData<String> OWNER_NAME =
            DataTracker.registerData(InfectedZombieEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> OWNER_UUID =
            DataTracker.registerData(InfectedZombieEntity.class, TrackedDataHandlerRegistry.STRING);

    public InfectedZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(OWNER_NAME, "");
        builder.add(OWNER_UUID, "");
    }

    public String getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID);
    }

    public String getOwnerName() {
        return this.dataTracker.get(OWNER_NAME);
    }

    public void setOwner(UUID id, String name) {
        this.dataTracker.set(OWNER_UUID, id.toString());
        this.dataTracker.set(OWNER_NAME, name);
        applyName();
    }

    /** Nombre en rojo + icono de zombie (glifo de la fuente permadeath:icons). */
    private void applyName() {
        Text icon = Text.literal("\uE000").setStyle(
                Style.EMPTY.withFont(new Identifier(PermadeathMod.MOD_ID, "icons")).withColor(Formatting.WHITE));
        this.setCustomName(Text.literal(getOwnerName() + " ").formatted(Formatting.RED).append(icon));
        this.setCustomNameVisible(true);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("OwnerName", getOwnerName());
        nbt.putString("OwnerUuid", getOwnerUuid());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("OwnerUuid") && nbt.contains("OwnerName")) {
            this.dataTracker.set(OWNER_UUID, nbt.getString("OwnerUuid"));
            this.dataTracker.set(OWNER_NAME, nbt.getString("OwnerName"));
            applyName();
        }
    }

    /** Genera el zombie en la posicion del jugador que acaba de morir infectado. */
    public static void spawnFor(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        InfectedZombieEntity zombie = ModEntities.INFECTED_ZOMBIE.create(world);
        if (zombie == null) {
            return;
        }
        zombie.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0f);
        zombie.setOwner(player.getUuid(), player.getGameProfile().getName());

        // dos filas de corazones extra (+40 de vida sobre los 20 normales)
        EntityAttributeInstance health = zombie.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(60.0);
        }
        zombie.setHealth(60.0f);
        zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, StatusEffectInstance.INFINITE, 0, false, false));
        zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE, 0, false, false));
        zombie.setPersistent();
        world.spawnEntity(zombie);
    }
}
