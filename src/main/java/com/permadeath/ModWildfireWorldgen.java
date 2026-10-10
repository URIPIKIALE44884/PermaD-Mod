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
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

/** Registro de la Fortaleza del Wildfire como feature, en cualquier bioma del Nether. */
public final class ModWildfireWorldgen {
    private ModWildfireWorldgen() {}

    private static final Identifier WILDFIRE_ID = new Identifier(PermadeathMod.MOD_ID, "wildfire_fortress");

    public static final Feature<DefaultFeatureConfig> WILDFIRE_FORTRESS = Registry.register(
            Registries.FEATURE, WILDFIRE_ID,
            new WildfireFortressFeature(DefaultFeatureConfig.CODEC));

    /*
     * BiomeModifications necesita que exista la entrada en PLACED_FEATURE.
     * Registrar solamente la Feature no crea automáticamente las entradas
     * CONFIGURED_FEATURE ni PLACED_FEATURE, y provocaba el crash al crear mundos.
     */
    private static final ConfiguredFeature<DefaultFeatureConfig, ?> WILDFIRE_CONFIGURED =
            Registry.register(Registries.CONFIGURED_FEATURE, WILDFIRE_ID,
                    new ConfiguredFeature<>(WILDFIRE_FORTRESS, DefaultFeatureConfig.INSTANCE));

    public static final PlacedFeature WILDFIRE_PLACED = Registry.register(
            Registries.PLACED_FEATURE, WILDFIRE_ID,
            new PlacedFeature(RegistryEntry.of(WILDFIRE_CONFIGURED), List.of()));

    public static void init() {
        RegistryKey<PlacedFeature> key = RegistryKey.of(RegistryKeys.PLACED_FEATURE, WILDFIRE_ID);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(),
                GenerationStep.Feature.SURFACE_STRUCTURES, key);
    }
}
