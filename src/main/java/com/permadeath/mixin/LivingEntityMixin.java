package com.permadeath.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.permadeath.ModEffects;

import net.minecraft.entity.LivingEntity;

/** Con Zombificacion no se puede recuperar vida (regeneracion natural, pociones, comida, etc.). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void permadeath$noHealWhenInfected(float amount, CallbackInfo ci) {
        if (((LivingEntity) (Object) this).hasStatusEffect(ModEffects.ZOMBIFICACION)) {
            ci.cancel();
        }
    }
}
