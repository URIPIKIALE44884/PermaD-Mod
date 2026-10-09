package com.permadeath;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Zombificacion: efecto de 5 minutos (configurable). No hace nada por si mismo:
 * - el mixin LivingEntityMixin bloquea toda curacion mientras dure,
 * - InfectionManager mata al jugador cuando llega a 0.
 */
public class ZombificationEffect extends StatusEffect {
    public ZombificationEffect() {
        super(StatusEffectCategory.HARMFUL, 0x3DA33D);
    }
}
