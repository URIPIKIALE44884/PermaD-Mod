package com.permadeath;

import java.util.List;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Clic derecho: gasta 1 corazon permanente a cambio de curacion total,
 * saturacion, absorcion (2 corazones dorados), regeneracion II (10 s) y
 * velocidad II (20 s). No se puede usar si ya estas en el minimo de 5 corazones.
 */
public class EssenceItem extends Item {
    public EssenceItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient || !(user instanceof ServerPlayerEntity player)) {
            return TypedActionResult.pass(stack);
        }

        if (player.hasStatusEffect(ModEffects.ZOMBIFICACION)) {
            player.sendMessage(Text.translatable("msg.permadeath.infected_no_essence"), true);
            return TypedActionResult.fail(stack);
        }

        int extra = HeartData.get(player);
        if (extra <= HeartData.MIN) {
            player.sendMessage(Text.translatable("msg.permadeath.hearts_min"), true);
            return TypedActionResult.fail(stack);
        }

        HeartData.set(player, extra - 1);

        player.setHealth(player.getMaxHealth());
        player.getHungerManager().add(20, 1.0f);
        // Absorcion I = 4 HP = 2 corazones dorados (120 s)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120 * 20, 0));
        // Regeneracion II por 10 s
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 10 * 20, 1));
        // Velocidad II por 20 s
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 20, 1));

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.PLAYERS, 1.0f, 1.4f);
        return TypedActionResult.success(stack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.permadeath.esencia_vital.tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.permadeath.esencia_vital.tooltip2").formatted(Formatting.DARK_GRAY));
    }
}
