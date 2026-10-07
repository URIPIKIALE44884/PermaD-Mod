package com.permadeath.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.permadeath.MobSpawnHandler;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

/**
 * MobEntity.initialize solo se llama cuando un mob se CREA (spawn, huevo, comando),
 * no cuando se carga desde el disco. Asi distinguimos mobs nuevos de mobs existentes.
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "initialize", at = @At("RETURN"))
    private void permadeath$markFresh(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
            EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        MobEntity self = (MobEntity) (Object) this;
        self.addCommandTag(MobSpawnHandler.TAG_FRESH);
        if (spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION) {
            self.addCommandTag(MobSpawnHandler.TAG_NATURAL);
        }
    }
}
