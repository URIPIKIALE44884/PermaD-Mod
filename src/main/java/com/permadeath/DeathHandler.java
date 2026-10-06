package com.permadeath;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

/**
 * - Cada muerte de un jugador resta 1 corazon permanente (hasta el minimo de 5).
 * - Si el jugador lleva un Totem de Memoria, se consume y SOLO ese jugador
 *   conserva inventario y experiencia al morir.
 *
 * Truco: justo antes de morir se activa keepInventory y se restaura apenas
 * termina la muerte (todo ocurre en el mismo tick del servidor). Al reaparecer,
 * se copia el inventario manualmente.
 */
public final class DeathHandler {
    private DeathHandler() {}

    private static final Set<UUID> KEEPERS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Boolean> PREVIOUS_RULE = new ConcurrentHashMap<>();

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && consumeTotem(player)) {
                KEEPERS.add(player.getUuid());
                GameRules.BooleanRule rule = player.getServerWorld().getGameRules().get(GameRules.KEEP_INVENTORY);
                PREVIOUS_RULE.put(player.getUuid(), rule.get());
                rule.set(true, player.getServer());
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }
            // restaurar la regla keepInventory
            Boolean previous = PREVIOUS_RULE.remove(player.getUuid());
            if (previous != null) {
                player.getServerWorld().getGameRules().get(GameRules.KEEP_INVENTORY).set(previous, player.getServer());
            }
            // perder un corazon permanente
            int extra = HeartData.get(player);
            if (extra > HeartData.MIN) {
                player.setAttached(HeartData.EXTRA, extra - 1);
            }
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (KEEPERS.remove(oldPlayer.getUuid())) {
                newPlayer.getInventory().clone(oldPlayer.getInventory());
                newPlayer.experienceLevel = oldPlayer.experienceLevel;
                newPlayer.totalExperience = oldPlayer.totalExperience;
                newPlayer.experienceProgress = oldPlayer.experienceProgress;
                newPlayer.setScore(oldPlayer.getScore());
            }
        });
    }

    private static boolean consumeTotem(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(ModItems.TOTEM)) {
                stack.decrement(1);
                return true;
            }
        }
        return false;
    }
}
