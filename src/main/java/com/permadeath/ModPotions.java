package com.permadeath;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Pocion Antidoto (se ve como cualquier pocion, teñida de dorado).
 * Se prepara en el soporte para pociones: pocion de curacion o de regeneracion
 * (cualquier nivel) + carne podrida. Con polvora sale arrojadiza, y con aliento de dragon, persistente.
 */
public final class ModPotions {
    private ModPotions() {}

    public static final RegistryEntry<Potion> ANTIDOTO = Registry.registerReference(
            Registries.POTION, new Identifier(PermadeathMod.MOD_ID, "antidoto"),
            new Potion(new StatusEffectInstance(ModEffects.ANTIDOTO, 1)));

    public static void init() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.HEALING, Items.ROTTEN_FLESH, ANTIDOTO);
            builder.registerPotionRecipe(Potions.STRONG_HEALING, Items.ROTTEN_FLESH, ANTIDOTO);
            builder.registerPotionRecipe(Potions.REGENERATION, Items.ROTTEN_FLESH, ANTIDOTO);
            builder.registerPotionRecipe(Potions.LONG_REGENERATION, Items.ROTTEN_FLESH, ANTIDOTO);
            builder.registerPotionRecipe(Potions.STRONG_REGENERATION, Items.ROTTEN_FLESH, ANTIDOTO);
        });
    }
}
