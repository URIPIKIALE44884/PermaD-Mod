package com.permadeath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Efecto instantaneo de la pocion Antidoto: quita la Zombificacion. */
public class AntidoteEffect extends InstantStatusEffect {
    public AntidoteEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFC83D);
    }

    @Override
    public void applyInstantEffect(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity) {
        target.removeStatusEffect(ModEffects.ZOMBIFICACION);
    }
}
