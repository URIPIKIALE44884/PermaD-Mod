package com.permadeath;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<InfectedZombieEntity> INFECTED_ZOMBIE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(PermadeathMod.MOD_ID, "infected_zombie"),
            EntityType.Builder.create(InfectedZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f)
                    .maxTrackingRange(8)
                    .build("infected_zombie"));

    public static final EntityType<IceologerEntity> ICEOLOGER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(PermadeathMod.MOD_ID, "iceologer"),
            EntityType.Builder.create(IceologerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.8f)
                    .maxTrackingRange(8)
                    .build("iceologer"));

    public static final EntityType<BlazeFatuoEntity> BLAZE_FATUO = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(PermadeathMod.MOD_ID, "blaze_fatuo"),
            EntityType.Builder.create(BlazeFatuoEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.8f)
                    .maxTrackingRange(8)
                                        .build("blaze_fatuo"));

    public static final EntityType<WildfireEntity> WILDFIRE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(PermadeathMod.MOD_ID, "wildfire"),
            EntityType.Builder.create(WildfireEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.8f, 1.8f)
                    .maxTrackingRange(10)
                    .fireImmune()
                    .build("wildfire"));

    public static final EntityType<SoulBlazeFireballEntity> SOUL_BLAZE_FIREBALL = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(PermadeathMod.MOD_ID, "soul_blaze_fireball"),
            EntityType.Builder.<SoulBlazeFireballEntity>create(SoulBlazeFireballEntity::new, SpawnGroup.MISC)
                    .dimensions(0.3125f, 0.3125f)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build("soul_blaze_fireball"));

    public static void init() {
        FabricDefaultAttributeRegistry.register(INFECTED_ZOMBIE, ZombieEntity.createZombieAttributes());
        FabricDefaultAttributeRegistry.register(ICEOLOGER, IceologerEntity.createIceologerAttributes());
        FabricDefaultAttributeRegistry.register(BLAZE_FATUO, BlazeFatuoEntity.createBlazeFatuoAttributes());
        FabricDefaultAttributeRegistry.register(WILDFIRE, WildfireEntity.createWildfireAttributes());
    }
}
