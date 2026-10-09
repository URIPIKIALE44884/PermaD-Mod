package com.permadeath.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.permadeath.WaveManager;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;

/**
 * Creepers de la oleada: la explosion no destruye bloques y solo hace dano a los jugadores
 * (no a otros mobs). Los creepers normales no se tocan.
 */
@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @Shadow
    private int explosionRadius;

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void permadeath$waveExplosion(CallbackInfo ci) {
        CreeperEntity self = (CreeperEntity) (Object) this;
        if (!self.getCommandTags().contains(WaveManager.TAG)) {
            return;
        }
        World world = self.getWorld();
        if (!world.isClient) {
            world.createExplosion(self, null, WaveManager.PLAYER_ONLY_EXPLOSION,
                    self.getX(), self.getY(), self.getZ(),
                    this.explosionRadius, false, World.ExplosionSourceType.NONE);
            self.discard();
        }
        ci.cancel();
    }
}
