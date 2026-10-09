package com.permadeath;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Corazones permanentes por jugador.
 * Se guarda "extra" en [-5, +10] (0 = 10 corazones). Vida maxima = 20 + 2 * extra,
 * o sea de 5 a 20 corazones.
 */
public final class HeartData {
    private HeartData() {}

    /** Minimo normal (5 corazones): lo usan la Esencia Vital y las muertes comunes. */
    public static final int MIN = -5;
    /** Minimo real (3 corazones): solo se llega por morir con Zombificacion. */
    public static final int ABS_MIN = -7;
    public static final int MAX = 10;

    public static final AttachmentType<Integer> EXTRA = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> 0)
            .buildAndRegister(new Identifier(PermadeathMod.MOD_ID, "extra_hearts"));

    public static int get(ServerPlayerEntity player) {
        return player.getAttachedOrElse(EXTRA, 0);
    }

    public static void set(ServerPlayerEntity player, int value) {
        player.setAttached(EXTRA, MathHelper.clamp(value, ABS_MIN, MAX));
        apply(player);
    }

    /** Aplica la vida maxima segun los corazones permanentes. */
    public static void apply(ServerPlayerEntity player) {
        EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(20.0 + 2.0 * get(player));
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> apply(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            apply(newPlayer);
            if (!alive) {
                newPlayer.setHealth(newPlayer.getMaxHealth());
            }
        });
    }
}
