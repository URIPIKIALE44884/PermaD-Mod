package com.permadeath;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEffects {
    private ModEffects() {}

    public static final RegistryEntry<StatusEffect> ZOMBIFICACION = Registry.registerReference(
            Registries.STATUS_EFFECT, new Identifier(PermadeathMod.MOD_ID, "zombificacion"), new ZombificationEffect());

    public static final RegistryEntry<StatusEffect> ANTIDOTO = Registry.registerReference(
            Registries.STATUS_EFFECT, new Identifier(PermadeathMod.MOD_ID, "antidoto"), new AntidoteEffect());

    public static void init() {}
}
