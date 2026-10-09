package com.permadeath;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.tag.ItemTags;

/**
 * Multishot para ARCOS: dispara entre 3 y 5 flechas (ver BowMultishot). Gasta una sola flecha.
 * Incompatible con Infinity y con Mending. Solo se consigue como botin del Ilusioner.
 */
public class BowMultishotEnchantment extends Enchantment {
    public BowMultishotEnchantment() {
        super(Enchantment.properties(ItemTags.BOW_ENCHANTABLE, 2, 1,
                Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EquipmentSlot.MAINHAND));
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return other != Enchantments.INFINITY && other != Enchantments.MENDING && super.canAccept(other);
    }

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
