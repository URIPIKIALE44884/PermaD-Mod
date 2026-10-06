package com.permadeath;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    private ModItemGroups() {}

    /**
     * Pestana propia del Creativo. Los items que estan en una pestana tambien
     * aparecen en la busqueda (la lupa) del inventario creativo.
     */
    public static final ItemGroup HARDCORE = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier(PermadeathMod.MOD_ID, "hardcore"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.ICONO_HARDCORE))
                    .displayName(Text.translatable("itemGroup.permadeath.hardcore"))
                    .entries((context, entries) -> {
                        entries.add(ModItems.CORAZON);
                        entries.add(ModItems.ESENCIA);
                        entries.add(ModItems.TOTEM);
                    })
                    .build());

    public static void init() {}
}
