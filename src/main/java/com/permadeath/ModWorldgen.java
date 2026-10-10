package com.permadeath;

import java.util.List;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * - Iglus del Iceologer: laderas nevadas y picos helados.
 * - Ilusioner solitario: bosque oscuro.
 * Solo afectan a chunks nuevos.
 */
public final class ModWorldgen {
    private ModWorldgen() {}

    private static final Identifier ICEOLOGER_ID = new Identifier(PermadeathMod.MOD_ID, "iceologer_igloo");
    private static final Identifier ILLUSIONER_ID = new Identifier(PermadeathMod.MOD_ID, "lone_illusioner");

    public static final Feature<DefaultFeatureConfig> ICEOLOGER_IGLOO = Registry.register(
            Registries.FEATURE, ICEOLOGER_ID,
            new IceologerIglooFeature(DefaultFeatureConfig.CODEC));

    public static final Feature<DefaultFeatureConfig> LONE_ILLUSIONER = Registry.register(
            Registries.FEATURE, ILLUSIONER_ID,
            new IllusionerFeature(DefaultFeatureConfig.CODEC));

    private static final ConfiguredFeature<DefaultFeatureConfig, ?> ICEOLOGER_CONFIGURED =
            Registry.register(Registries.CONFIGURED_FEATURE, ICEOLOGER_ID,
                    new ConfiguredFeature<>(ICEOLOGER_IGLOO, DefaultFeatureConfig.INSTANCE));

    private static final ConfiguredFeature<DefaultFeatureConfig, ?> ILLUSIONER_CONFIGURED =
            Registry.register(Registries.CONFIGURED_FEATURE, ILLUSIONER_ID,
                    new ConfiguredFeature<>(LONE_ILLUSIONER, DefaultFeatureConfig.INSTANCE));

    public static final PlacedFeature ICEOLOGER_PLACED = Registry.register(
            Registries.PLACED_FEATURE, ICEOLOGER_ID,
            new PlacedFeature(RegistryEntry.of(ICEOLOGER_CONFIGURED), List.of()));

    public static final PlacedFeature ILLUSIONER_PLACED = Registry.register(
            Registries.PLACED_FEATURE, ILLUSIONER_ID,
            new PlacedFeature(RegistryEntry.of(ILLUSIONER_CONFIGURED), List.of()));

    public static void init() {
        RegistryKey<PlacedFeature> igloo = RegistryKey.of(RegistryKeys.PLACED_FEATURE, ICEOLOGER_ID);
        RegistryKey<PlacedFeature> illusioner = RegistryKey.of(RegistryKeys.PLACED_FEATURE, ILLUSIONER_ID);

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.SNOWY_SLOPES, BiomeKeys.FROZEN_PEAKS),
                GenerationStep.Feature.SURFACE_STRUCTURES, igloo);
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST),
                GenerationStep.Feature.SURFACE_STRUCTURES, illusioner);
    }
}
