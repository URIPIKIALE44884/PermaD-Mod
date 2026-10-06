package com.permadeath;

import java.util.Set;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

/** Agrega el Corazon (1/16) y la Esencia Vital (1/24) a cofres de estructuras. */
public final class ChestLoot {
    private ChestLoot() {}

    private static final Set<Identifier> TARGETS = Set.of(
            new Identifier("minecraft", "chests/simple_dungeon"),
            new Identifier("minecraft", "chests/abandoned_mineshaft"),
            new Identifier("minecraft", "chests/desert_pyramid"),
            new Identifier("minecraft", "chests/jungle_temple"),
            new Identifier("minecraft", "chests/stronghold_corridor"));

    public static final float HEART_CHANCE = 1.0f / 16.0f;
    public static final float ESSENCE_CHANCE = 1.0f / 24.0f;

    public static void init() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin() || !TARGETS.contains(id)) {
                return;
            }
            tableBuilder.pool(pool(ModItems.CORAZON, HEART_CHANCE));
            tableBuilder.pool(pool(ModItems.ESENCIA, ESSENCE_CHANCE));
        });
    }

    private static LootPool.Builder pool(Item item, float chance) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0f))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(item));
    }
}
