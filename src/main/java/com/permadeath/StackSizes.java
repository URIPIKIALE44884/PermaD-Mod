package com.permadeath;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * - Las tres pociones (normal, arrojadiza y persistente) apilan hasta 32.
 * - Las comidas en tazon (estofado de hongos, sopa de remolacha, estofado de conejo
 *   y estofado sospechoso) apilan hasta 16.
 */
public final class StackSizes {
    private StackSizes() {}

    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            for (Item potion : new Item[] { Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION }) {
                context.modify(potion, builder -> builder.add(DataComponentTypes.MAX_STACK_SIZE, 32));
            }
            for (Item bowlFood : new Item[] { Items.MUSHROOM_STEW, Items.BEETROOT_SOUP,
                    Items.RABBIT_STEW, Items.SUSPICIOUS_STEW }) {
                context.modify(bowlFood, builder -> builder.add(DataComponentTypes.MAX_STACK_SIZE, 16));
            }
        });
    }
}
