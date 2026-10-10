package com.permadeath;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.ItemTags;

/**
 * Ice Aspect (espadas). Al golpear:
 * - nivel I: Lentitud I y congelamiento (como la nieve en polvo) durante 5 segundos;
 * - nivel II: Lentitud II y congelamiento durante 10 segundos.
 * Se obtiene del Iceologer (libro Ice Aspect I) y se sube a II combinando dos I en un yunque.
 * No es compatible con Aspecto de Fuego.
 */
public class IceAspectEnchantment extends Enchantment {
    public IceAspectEnchantment() {
        super(Enchantment.properties(ItemTags.SWORD_ENCHANTABLE, 2, 2,
                Enchantment.leveledCost(10, 20), Enchantment.leveledCost(60, 20), 4, EquipmentSlot.MAINHAND));
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return other != Enchantments.FIRE_ASPECT && super.canAccept(other);
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        if (user.getWorld().isClient || !(target instanceof LivingEntity living)) {
            return;
        }
        int seconds = level >= 2 ? 10 : 5;
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, seconds * 20, Math.max(0, level - 1)), user);
        if (living.canFreeze()) {
            // el contador de congelamiento baja 2 por tick: este valor mantiene al mob congelado 'seconds' segundos
            living.setFrozenTicks(living.getMinFreezeDamageTicks() + seconds * 40);
        }
    }

    /** Solo se consigue como botin del Iceologer: no aparece en la mesa de encantamientos ni en aldeanos. */
    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
