package com.permadeath;

import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModItems {
    private ModItems() {}

    /** +1 corazon permanente (maximo 20 corazones). */
    public static final Item CORAZON = register("corazon_permanente",
            new HeartItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));

    /** Cambia 1 corazon permanente por curacion total y efectos. */
    public static final Item ESENCIA = register("esencia_vital",
            new EssenceItem(new Item.Settings().maxCount(16).rarity(Rarity.EPIC)));

    /** Conserva el inventario al morir (por jugador) y se consume. */
    public static final Item TOTEM = register("totem_memoria",
            new MemoryTotemItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

    /** Solo icono de la pestana del Creativo. No esta en ninguna pestana. */
    public static final Item ICONO_HARDCORE = register("icono_hardcore",
            new Item(new Item.Settings().maxCount(1)));

    /** Huevo de aparicion del Iceologer. */
    public static final Item ICEOLOGER_SPAWN_EGG = register("iceologer_spawn_egg",
            new SpawnEggItem(ModEntities.ICEOLOGER, 0x2F5F9F, 0xBFE8FF, new Item.Settings()));

    /** Gema del Wildfire: ingrediente de su Cresta. Resistente al fuego. */
    public static final Item WILDFIRE_GEM = register("wildfire_gem",
            new WildfireGemItem(new Item.Settings().fireproof().rarity(Rarity.RARE)));

    /** Cresta del Wildfire: casco que da Resistencia al Fuego mientras se usa. */
    public static final Item WILDFIRE_CREST = register("wildfire_crest",
            new WildfireCrestItem(new Item.Settings().fireproof().maxDamage(300).rarity(Rarity.EPIC)));

    public static final Item BLAZE_FATUO_SPAWN_EGG = register("blaze_fatuo_spawn_egg",
            new SpawnEggItem(ModEntities.BLAZE_FATUO, 0x8FE8FF, 0x1B3A80, new Item.Settings()));

    public static final Item WILDFIRE_SPAWN_EGG = register("wildfire_spawn_egg",
            new SpawnEggItem(ModEntities.WILDFIRE, 0xD2461E, 0xFFAA28, new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(PermadeathMod.MOD_ID, name), item);
    }

    /** Fuerza la carga de la clase (y con eso el registro de los items). */
    public static void init() {}
}
