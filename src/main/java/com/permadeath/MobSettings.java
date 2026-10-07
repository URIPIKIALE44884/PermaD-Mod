package com.permadeath;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuracion de un mob. Todo en 0 / vanilla por defecto.
 * Se guarda como JSON, por eso los campos son publicos y simples.
 */
public class MobSettings {
    /** % de mobs nuevos que reciben cambios. */
    public int chance = 100;

    /** Probabilidad (%) de los niveles de armadura 1, 2 y 3 (hierro, diamante, netherita). */
    public int[] armorChance = new int[] { 0, 0, 0 };

    /** Probabilidad (%) del equipo especial (zombies: arco; esqueletos: escudo + espada de piedra). */
    public int specialChance = 0;

    /** Si es true, el equipo agregado puede soltarse con la probabilidad normal del juego. */
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
        return s;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Completa y limpia valores (JSON viejo, editado a mano o enviado por la red). */
    public void fix(MobKind kind) {
        chance = clamp(chance, 0, 100);
        specialChance = clamp(specialChance, 0, 100);
        if (armorChance == null || armorChance.length < 3) {
            armorChance = new int[] { 0, 0, 0 };
        }
        for (int i = 0; i < armorChance.length; i++) {
            armorChance[i] = clamp(armorChance[i], 0, 100);
        }
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
