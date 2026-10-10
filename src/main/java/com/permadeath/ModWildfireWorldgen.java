package com.permadeath;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

/** Registro de la Fortaleza del Wildfire como feature, en cualquier bioma del Nether. */
public final class ModWildfireWorldgen {
    private ModWildfireWorldgen() {}

    public static final Feature<DefaultFeatureConfig> WILDFIRE_FORTRESS = Registry.register(
            Registries.FEATURE,
            new Identifier(PermadeathMod.MOD_ID, "wildfire_fortress"),
            new WildfireFortressFeature(DefaultFeatureConfig.CODEC));

    public static void init() {
        RegistryKey<PlacedFeature> key = RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                new Identifier(PermadeathMod.MOD_ID, "wildfire_fortress"));
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(),
                GenerationStep.Feature.SURFACE_STRUCTURES, key);
    }
}
