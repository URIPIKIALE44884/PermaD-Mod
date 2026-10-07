package com.permadeath;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
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

    public static void init() {
        FabricDefaultAttributeRegistry.register(INFECTED_ZOMBIE, ZombieEntity.createZombieAttributes());
    }
}
