package com.permadeath.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.mob.CreeperEntity;

@Mixin(CreeperEntity.class)
public interface CreeperEntityAccessor {
    @Accessor("explosionRadius")
    void permadeath$setExplosionRadius(int radius);
}
