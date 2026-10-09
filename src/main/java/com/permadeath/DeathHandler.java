package com.permadeath;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;

/**
 * - Cada muerte resta 1 corazon permanente (hasta el minimo de 5).
 * - Si el jugador muere con Zombificacion pierde 2 corazones EN TOTAL (en vez de 1),
 *   y puede bajar hasta 3 corazones (minimo real).
 * - Si lleva un Totem de Memoria EN LA MANO (principal o secundaria), se consume y SOLO ese
 *   jugador conserva inventario y experiencia. Al reaparecer ve su propia animacion de totem.
 *
 * Truco del totem: justo antes de morir se activa keepInventory y se restaura apenas
 * termina la muerte (mismo tick del servidor). Al reaparecer se copia el inventario.
 */
public final class DeathHandler {
    private DeathHandler() {}

    private static final Set<UUID> KEEPERS = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ANIMATE = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> INFECTED = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Boolean> PREVIOUS_RULE = new ConcurrentHashMap<>();

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (player.hasStatusEffect(ModEffects.ZOMBIFICACION)) {
                    INFECTED.add(player.getUuid());
                }
                if (consumeTotem(player)) {
                    KEEPERS.add(player.getUuid());
                    GameRules.BooleanRule rule = player.getServerWorld().getGameRules().get(GameRules.KEEP_INVENTORY);
                    PREVIOUS_RULE.put(player.getUuid(), rule.get());
                    rule.set(true, player.getServer());
                }
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }
            Boolean previous = PREVIOUS_RULE.remove(player.getUuid());
            if (previous != null) {
                player.getServerWorld().getGameRules().get(GameRules.KEEP_INVENTORY).set(previous, player.getServer());
            }

            WaveManager.cancel(player.getUuid(), player.getServer());

            int extra = HeartData.get(player);
            if (INFECTED.remove(player.getUuid())) {
                player.setAttached(HeartData.EXTRA, Math.max(HeartData.ABS_MIN, extra - 2));
                // muerte con Zombificacion (por cualquier causa): aparece el zombie con la skin del jugador
                InfectedZombieEntity.spawnFor(player);
            } else if (extra > HeartData.MIN) {
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
                ANIMATE.add(newPlayer.getUuid());
            }
        });

        // animacion propia del totem, ya con el jugador reaparecido
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (ANIMATE.remove(newPlayer.getUuid())) {
                ServerPlayNetworking.send(newPlayer, new TotemAnimationPayload());
            }
        });
    }

    /** El totem solo funciona si esta en la mano principal o en la secundaria. */
    private static boolean consumeTotem(ServerPlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(ModItems.TOTEM)) {
                stack.decrement(1);
                return true;
            }
        }
        return false;
    }
}
