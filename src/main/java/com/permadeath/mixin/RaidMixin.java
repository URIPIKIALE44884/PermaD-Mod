package com.permadeath.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.permadeath.GlobalSettings;
import com.permadeath.ModConfig;
import com.permadeath.ModEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;

/**
 * Cada vez que la raid genera una oleada, el Iceologer y el Ilusioner pueden sumarse con la
 * probabilidad (independiente) configurada en el menu. require = 0: si el metodo cambiara de nombre,
 * el juego no se cierra, solo no aparecen en raids.
 */
@Mixin(Raid.class)
public abstract class RaidMixin {
    @Inject(method = "spawnNextWave", at = @At("TAIL"), require = 0)
    private void permadeath$extraRaiders(BlockPos pos, CallbackInfo ci) {
        Raid raid = (Raid) (Object) this;
        GlobalSettings settings = ModConfig.global();
        if (settings.iceologerRaidChance <= 0 && settings.illusionerRaidChance <= 0) {
            return;
        }
        Set<RaiderEntity> raiders = raid.getAllRaiders();
        if (raiders.isEmpty()) {
            return;
        }
        RaiderEntity sample = raiders.iterator().next();
        if (!(sample.getWorld() instanceof ServerWorld world)) {
            return;
        }
        Random random = world.getRandom();
        int wave = raid.getGroupsSpawned();
        if (random.nextInt(100) < settings.iceologerRaidChance) {
            permadeath$addExtra(raid, world, ModEntities.ICEOLOGER, wave, pos);
        }
        if (random.nextInt(100) < settings.illusionerRaidChance) {
            permadeath$addExtra(raid, world, EntityType.ILLUSIONER, wave, pos);
        }
    }

    private static <T extends RaiderEntity> void permadeath$addExtra(Raid raid, ServerWorld world,
            EntityType<T> type, int wave, BlockPos pos) {
        T raider = type.create(world);
        if (raider != null) {
            raid.addRaider(wave, raider, pos, false);
        }
    }
}
