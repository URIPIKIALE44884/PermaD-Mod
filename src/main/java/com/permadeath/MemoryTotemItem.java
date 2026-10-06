package com.permadeath;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** No hace nada al usarlo: basta con llevarlo en el inventario al morir. */
public class MemoryTotemItem extends Item {
    public MemoryTotemItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.permadeath.totem_memoria.tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.permadeath.totem_memoria.tooltip2").formatted(Formatting.DARK_GRAY));
    }
}
