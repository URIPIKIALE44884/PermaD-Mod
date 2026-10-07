package com.permadeath;

import net.minecraft.item.Item;
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

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(PermadeathMod.MOD_ID, name), item);
    }

    /** Fuerza la carga de la clase (y con eso el registro de los items). */
    public static void init() {}
}
