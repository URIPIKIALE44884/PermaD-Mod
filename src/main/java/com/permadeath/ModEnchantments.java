package com.permadeath;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {
    private ModEnchantments() {}

    public static final Enchantment ICE_ASPECT = Registry.register(Registries.ENCHANTMENT,
            new Identifier(PermadeathMod.MOD_ID, "ice_aspect"), new IceAspectEnchantment());

    public static final Enchantment BOW_MULTISHOT = Registry.register(Registries.ENCHANTMENT,
            new Identifier(PermadeathMod.MOD_ID, "bow_multishot"), new BowMultishotEnchantment());

    public static void init() {}
}
