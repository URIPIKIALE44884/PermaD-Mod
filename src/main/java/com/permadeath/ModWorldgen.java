package com.permadeath;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Iglus del Iceologer en biomas nevados e Illusioner solitario en bosque oscuro.
 * Solo afecta a chunks nuevos.
 */
public final class ModWorldgen {
    private ModWorldgen() {}

    public static final Feature<DefaultFeatureConfig> ICEOLOGER_IGLOO = Registry.register(
            Registries.FEATURE,
            new Identifier(PermadeathMod.MOD_ID, "iceologer_igloo"),
            new IceologerIglooFeature(DefaultFeatureConfig.CODEC));

    public static final Feature<DefaultFeatureConfig> LONE_ILLUSIONER = Registry.register(
            Registries.FEATURE,
            new Identifier(PermadeathMod.MOD_ID, "lone_illusioner"),
            new IllusionerFeature(DefaultFeatureConfig.CODEC));

    public static void init() {
        RegistryKey<PlacedFeature> igloo = RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                new Identifier(PermadeathMod.MOD_ID, "iceologer_igloo"));
        RegistryKey<PlacedFeature> illusioner = RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                new Identifier(PermadeathMod.MOD_ID, "lone_illusioner"));

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.SNOWY_SLOPES, BiomeKeys.FROZEN_PEAKS),
                GenerationStep.Feature.SURFACE_STRUCTURES, igloo);
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST),
                GenerationStep.Feature.SURFACE_STRUCTURES, illusioner);
    }
}
