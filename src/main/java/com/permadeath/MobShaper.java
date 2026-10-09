package com.permadeath;

import java.util.Map;

import com.permadeath.mixin.CreeperEntityAccessor;

import net.minecraft.enchantment.Enchantments;
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

    /** Piezas, en el orden de ARMOR_SLOTS y ARMOR_ITEMS. */
    public static final String[] ARMOR_KEYS = { "head", "chest", "legs", "feet" };

    /** Materiales, en el orden de ARMOR_ITEMS. */
    public static final String[] MATERIAL_KEYS = { "leather", "chainmail", "gold", "iron", "diamond", "netherite" };

    public static final int NETHERITE = 5;

    public static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    /** [pieza][material]: cuero, malla, oro, hierro, diamante, netherita. */
    public static final Item[][] ARMOR_ITEMS = {
            { Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.GOLDEN_HELMET, Items.IRON_HELMET,
                    Items.DIAMOND_HELMET, Items.NETHERITE_HELMET },
            { Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE,
                    Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE },
            { Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.IRON_LEGGINGS,
                    Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS },
            { Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.GOLDEN_BOOTS, Items.IRON_BOOTS,
                    Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS } };

    public static void apply(MobEntity mob, MobKind kind, MobSettings s) {
        Random random = mob.getRandom();
        if (random.nextInt(100) >= s.chance) {
            return;
        }

        if (kind.family == MobKind.Family.ZOMBIE || kind.family == MobKind.Family.SKELETON) {
            applyArmor(mob, kind, s, random);
            applySpecial(mob, kind, s, random);
        }

        if (kind.family == MobKind.Family.CREEPER && mob instanceof CreeperEntity creeper && s.creeperRadius != 3) {
            ((CreeperEntityAccessor) (Object) creeper).permadeath$setExplosionRadius(s.creeperRadius);
        }

        applyEffects(mob, s, random);
    }

    /**
     * Probabilidad de que el equipo se suelte al morir.
     * Se usa -1 (nunca) en vez de 0 porque con Looting una probabilidad de 0 todavia puede soltar el objeto.
     * Las armaduras que ese mob no podria tener de forma natural (netherita en zombies) NUNCA se sueltan,
     * y como el valor se copia al convertirse, tampoco si el zombie pasa a ser ahogado.
     */
    private static float dropChance(MobKind kind, MobSettings s, int material) {
        if (kind.family == MobKind.Family.ZOMBIE && material == NETHERITE) {
            return -1.0f;
        }
        return s.dropEquipment ? 0.085f : -1.0f;
    }

    private static void applyArmor(MobEntity mob, MobKind kind, MobSettings s, Random random) {
        for (int slot = 0; slot < ARMOR_KEYS.length; slot++) {
            int[] chances = s.armor.get(ARMOR_KEYS[slot]);
            if (chances == null) {
                continue;
            }
            boolean configured = false;
            for (int c : chances) {
                if (c >= 0) {
                    configured = true;
                }
            }
            if (!configured) {
                continue; // pieza sin configurar: queda como en vanilla
            }

            // las probabilidades de los materiales se suman; lo que falte para 100 = sin pieza
            int roll = random.nextInt(100);
            int cumulative = 0;
            int chosen = -1;
            for (int m = 0; m < chances.length; m++) {
                if (chances[m] <= 0) {
                    continue;
                }
                cumulative += chances[m];
                if (roll < cumulative) {
                    chosen = m;
                    break;
                }
            }

            EquipmentSlot equipmentSlot = ARMOR_SLOTS[slot];
            if (chosen < 0) {
                mob.equipStack(equipmentSlot, ItemStack.EMPTY);
                continue;
            }

            ItemStack stack = new ItemStack(ARMOR_ITEMS[slot][chosen]);
            if (s.armorEnchantChance > 0 && random.nextInt(100) < s.armorEnchantChance) {
                stack.addEnchantment(Enchantments.PROTECTION, protectionLevel(random));
            }
            mob.equipStack(equipmentSlot, stack);
            mob.setEquipmentDropChance(equipmentSlot, dropChance(kind, s, chosen));
        }
    }

    /** Proteccion I-IV: cuanto mayor el nivel, menos probable (50% / 30% / 15% / 5%). */
    private static int protectionLevel(Random random) {
        int roll = random.nextInt(100);
        if (roll < 50) {
            return 1;
        }
        if (roll < 80) {
            return 2;
        }
        if (roll < 95) {
            return 3;
        }
        return 4;
    }

    private static void applySpecial(MobEntity mob, MobKind kind, MobSettings s, Random random) {
        if (s.specialChance <= 0 || random.nextInt(100) >= s.specialChance) {
            return;
        }
        float drop = s.dropEquipment ? 0.085f : -1.0f;
        if (kind.family == MobKind.Family.ZOMBIE) {
            // arco (la IA para que dispare llega mas adelante)
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
