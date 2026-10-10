package com.permadeath;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Cresta del Wildfire: casco que da Resistencia al Fuego mientras se lleva puesto. */
public class WildfireCrestItem extends ArmorItem {
    public WildfireCrestItem(Settings settings) {
        super(ArmorMaterials.IRON, Type.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!world.isClient && entity instanceof net.minecraft.entity.LivingEntity living
                && living.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD) == stack) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 220, 0, true, false));
        }
    }
}
