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
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;

/** Permite que Iceologers e Ilusioners se sumen a las oleadas con probabilidades configurables. */
@Mixin(Raid.class)
public abstract class RaidMixin {
    private static final double ILLUSIONER_MAX_HEALTH = 60.0;

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
            if (raider instanceof IllusionerEntity illusioner) {
                illusioner.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(ILLUSIONER_MAX_HEALTH);
                illusioner.setHealth(ILLUSIONER_MAX_HEALTH);
            }
            raid.addRaider(wave, raider, pos, false);
        }
    }
}
