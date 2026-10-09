package com.permadeath;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/** Registro de paquetes y receptores del servidor. Solo los operadores (nivel 2) pueden cambiar la configuracion. */
public final class Networking {
    private Networking() {}

    public static void init() {
        PayloadTypeRegistry.playS2C().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(InfectionPayload.ID, InfectionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemAnimationPayload.ID, TotemAnimationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WavePayload.ID, WavePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WaveActionPayload.ID, WaveActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdatePayload.ID, UpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ResetPayload.ID, ResetPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdatePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(2)) {
                return;
            }
            try {
                if ("global".equals(payload.kind())) {
                    GlobalSettings global = ModConfig.parseGlobal(payload.json());
                    if (global != null) {
                        ModConfig.setGlobal(global);
                    }
                } else {
                    MobKind kind = MobKind.byId(payload.kind());
                    MobSettings settings = ModConfig.parseMob(payload.json());
                    if (kind != null && settings != null) {
                        ModConfig.setMob(kind, settings);
                    }
                }
            } catch (Exception e) {
                System.err.println("[permadeath] Paquete de configuracion invalido: " + e);
            }
            sendConfig(player, false);
        });

        ServerPlayNetworking.registerGlobalReceiver(WaveActionPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(2)) {
                return;
            }
            if ("start".equals(payload.action())) {
                int count = WaveManager.startAll(context.server());
                player.sendMessage(net.minecraft.text.Text.literal("Oleada iniciada para " + count + " jugador(es)."), false);
            } else if ("cancel".equals(payload.action())) {
                WaveManager.cancelAll(context.server());
                player.sendMessage(net.minecraft.text.Text.literal("Oleadas canceladas."), false);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ResetPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(2)) {
                return;
            }
            if ("*".equals(payload.kind())) {
                ModConfig.resetAll();
            } else {
                MobKind kind = MobKind.byId(payload.kind());
                if (kind != null) {
                    ModConfig.resetKind(kind);
                }
            }
            sendConfig(player, false);
        });
    }

    public static void sendConfig(ServerPlayerEntity player, boolean open) {
        ServerPlayNetworking.send(player, new ConfigPayload(ModConfig.toTransferJson(), open));
    }
}
