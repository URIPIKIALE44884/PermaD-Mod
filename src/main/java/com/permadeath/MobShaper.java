package com.permadeath;

import java.util.Map;

import com.permadeath.mixin.CreeperEntityAccessor;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;

/** Aplica la configuracion a un mob recien spawneado. */
public final class MobShaper {
    private MobShaper() {}

    public static final Map<String, RegistryEntry<StatusEffect>> EFFECTS = Map.of(
            "speed", StatusEffects.SPEED,
            "strength", StatusEffects.STRENGTH,
            "resistance", StatusEffects.RESISTANCE,
            "regeneration", StatusEffects.REGENERATION,
            "fire_resistance", StatusEffects.FIRE_RESISTANCE,
            "invisibility", StatusEffects.INVISIBILITY,
            "jump_boost", StatusEffects.JUMP_BOOST);

    public static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    /** Nivel 1 = hierro, nivel 2 = diamante, nivel 3 = netherita (set completo). */
    public static final Item[][] ARMOR = {
            { Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS },
            { Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS },
            { Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS } };

    public static void apply(MobEntity mob, MobKind kind, MobSettings s) {
        Random random = mob.getRandom();
        if (random.nextInt(100) >= s.chance) {
            return;
        }

        if (kind.family == MobKind.Family.ZOMBIE || kind.family == MobKind.Family.SKELETON) {
            applyArmor(mob, s, random);
            applySpecial(mob, kind, s, random);
        }

        if (kind.family == MobKind.Family.CREEPER && mob instanceof CreeperEntity creeper && s.creeperRadius != 3) {
            ((CreeperEntityAccessor) (Object) creeper).permadeath$setExplosionRadius(s.creeperRadius);
        }

        applyEffects(mob, s, random);
    }

    private static void applyArmor(MobEntity mob, MobSettings s, Random random) {
        // se prueba del nivel mas alto al mas bajo; el primero que sale, gana
        for (int level = 3; level >= 1; level--) {
            if (random.nextInt(100) < s.armorChance[level - 1]) {
                for (int i = 0; i < ARMOR_SLOTS.length; i++) {
                    mob.equipStack(ARMOR_SLOTS[i], new ItemStack(ARMOR[level - 1][i]));
                    mob.setEquipmentDropChance(ARMOR_SLOTS[i], s.dropEquipment ? 0.085f : 0.0f);
                }
                return;
            }
        }
    }

    private static void applySpecial(MobEntity mob, MobKind kind, MobSettings s, Random random) {
        if (s.specialChance <= 0 || random.nextInt(100) >= s.specialChance) {
            return;
        }
        float drop = s.dropEquipment ? 0.085f : 0.0f;
        if (kind.family == MobKind.Family.ZOMBIE) {
            // arco (la IA para que dispare llega en la etapa 4)
            mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            mob.setEquipmentDropChance(EquipmentSlot.MAINHAND, drop);
        } else {
            // espada de piedra y escudo
            mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            mob.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            mob.setEquipmentDropChance(EquipmentSlot.MAINHAND, drop);
            mob.setEquipmentDropChance(EquipmentSlot.OFFHAND, drop);
        }
    }

    private static void applyEffects(MobEntity mob, MobSettings s, Random random) {
        for (Map.Entry<String, int[]> entry : s.effects.entrySet()) {
            int[] value = entry.getValue();
            RegistryEntry<StatusEffect> effect = EFFECTS.get(entry.getKey());
            if (effect == null || value == null || value.length < 2 || value[0] <= 0) {
                continue;
            }
            if (random.nextInt(100) >= value[1]) {
                continue;
            }
            mob.addStatusEffect(new StatusEffectInstance(effect, StatusEffectInstance.INFINITE, value[0] - 1, false, false));
        }
    }
}
