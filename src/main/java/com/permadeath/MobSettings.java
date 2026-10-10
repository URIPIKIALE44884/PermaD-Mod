package com.permadeath;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuracion de un mob. Todo apagado / vanilla por defecto.
 * Se guarda como JSON, por eso los campos son publicos y simples.
 */
public class MobSettings {
    /** % de mobs nuevos que reciben cambios. */
    public int chance = 100;

    /**
     * Armadura por pieza (head, chest, legs, feet). Cada pieza tiene 6 materiales, en este orden:
     * cuero, malla, oro, hierro, diamante, netherita.
     * Valor -1 = material desactivado (X). 0..100 = probabilidad. La suma de una pieza no pasa de 100:
     * lo que falta para 100 es la probabilidad de que el mob salga SIN esa pieza.
     * Si una pieza tiene todos sus materiales en -1, queda como en vanilla.
     */
    public Map<String, int[]> armor = new LinkedHashMap<>();

    /** Probabilidad (%) de que una pieza de armadura salga encantada con Proteccion I-IV. */
    public int armorEnchantChance = 10;

    /** Probabilidad (%) del equipo especial (zombies: arco; esqueletos: escudo + espada de piedra). */
    public int specialChance = 0;

    /** Si es true, el equipo natural agregado puede soltarse con la probabilidad normal del juego. */
    public boolean dropEquipment = false;

    /** efecto -> [nivel 0..3, probabilidad %]. Nivel 0 = apagado. */
    public Map<String, int[]> effects = new LinkedHashMap<>();

    /** Dimensiones donde puede spawnear: overworld, nether, end. */
    public Set<String> allowedDims = new LinkedHashSet<>();

    /** Solo creeper: radio de explosion (3 = vanilla). */
    public int creeperRadius = 3;

    public static MobSettings defaultFor(MobKind kind) {
        MobSettings s = new MobSettings();
        s.allowedDims = new LinkedHashSet<>(kind.vanillaDims);
        s.fix(kind);
        return s;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Completa y limpia valores (JSON viejo, editado a mano o enviado por la red). */
    public void fix(MobKind kind) {
        chance = clamp(chance, 0, 100);
        specialChance = clamp(specialChance, 0, 100);
        armorEnchantChance = clamp(armorEnchantChance, 0, 100);

        Map<String, int[]> cleanedArmor = new LinkedHashMap<>();
        for (String key : MobShaper.ARMOR_KEYS) {
            int[] source = armor == null ? null : armor.get(key);
            int[] result = new int[6];
            int sum = 0;
            for (int i = 0; i < 6; i++) {
                int v = (source != null && i < source.length) ? clamp(source[i], -1, 100) : -1;
                if (v > 0) {
                    if (sum + v > 100) {
                        v = 100 - sum;
                    }
                    sum += v;
                }
                result[i] = v;
            }
            cleanedArmor.put(key, result);
        }
        armor = cleanedArmor;

        if (effects == null) {
            effects = new LinkedHashMap<>();
        }
        Map<String, int[]> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : effects.entrySet()) {
            int[] v = e.getValue();
            if (v != null && v.length >= 2 && MobShaper.EFFECTS.containsKey(e.getKey())) {
                cleaned.put(e.getKey(), new int[] { clamp(v[0], 0, 3), clamp(v[1], 0, 100) });
            }
        }
        effects = cleaned;

        Set<String> dims = new LinkedHashSet<>();
        if (allowedDims == null) {
            dims.addAll(kind.vanillaDims);
        } else {
            for (String d : allowedDims) {
                if (d.equals("overworld") || d.equals("nether") || d.equals("end")) {
                    dims.add(d);
                }
            }
        }
        allowedDims = dims;
        creeperRadius = clamp(creeperRadius, 1, 12);
    }
}
