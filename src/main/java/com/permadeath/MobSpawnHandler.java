package com.permadeath;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * - Un mixin marca con "pm_fresh" a los mobs que acaban de spawnear (no a los que se cargan
 *   desde el disco), asi los mobs que ya existen en el mundo no se tocan.
 * - Al cargarse en el mundo, se les aplica la configuracion.
 * - Spawns nuevos en otras dimensiones: se registran al iniciar el juego
 *   (un cambio de dimensiones requiere reiniciar el servidor).
 */
public final class MobSpawnHandler {
    private MobSpawnHandler() {}

    public static final String TAG_FRESH = "pm_fresh";
    public static final String TAG_NATURAL = "pm_natural";

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof MobEntity mob) || !mob.getCommandTags().contains(TAG_FRESH)) {
                return;
            }
            boolean natural = mob.getCommandTags().contains(TAG_NATURAL);
            mob.removeCommandTag(TAG_FRESH);
            mob.removeCommandTag(TAG_NATURAL);

            MobKind kind = MobKind.of(mob.getType());
            if (kind == null) {
                return;
            }
            MobSettings settings = ModConfig.get(kind);

            // restriccion: spawn natural en una dimension donde el mob normalmente aparece pero esta desactivado
            String dim = dimensionId(world);
            if (natural && dim != null && kind.vanillaDims.contains(dim) && !settings.allowedDims.contains(dim)) {
                mob.discard();
                return;
            }

            MobShaper.apply(mob, kind, settings);
        });

        registerExtraSpawns();
    }

    private static void registerExtraSpawns() {
        for (MobKind kind : MobKind.values()) {
            MobSettings settings = ModConfig.get(kind);
            for (String dim : settings.allowedDims) {
                if (kind.vanillaDims.contains(dim)) {
                    continue;
                }
                switch (dim) {
                    case "overworld" -> BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                            SpawnGroup.MONSTER, kind.type, 20, 1, 3);
                    case "nether" -> BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(),
                            SpawnGroup.MONSTER, kind.type, 20, 1, 3);
                    case "end" -> BiomeModifications.addSpawn(BiomeSelectors.foundInTheEnd(),
                            SpawnGroup.MONSTER, kind.type, 20, 1, 3);
                    default -> { }
                }
            }
        }
    }

    public static String dimensionId(ServerWorld world) {
        if (world.getRegistryKey() == World.OVERWORLD) {
            return "overworld";
        }
        if (world.getRegistryKey() == World.NETHER) {
            return "nether";
        }
        if (world.getRegistryKey() == World.END) {
            return "end";
        }
        return null;
    }
}
