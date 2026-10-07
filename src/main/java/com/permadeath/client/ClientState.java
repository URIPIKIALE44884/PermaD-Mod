package com.permadeath.client;

import com.permadeath.ConfigData;
import com.permadeath.ConfigPayload;
import com.permadeath.InfectionPayload;
import com.permadeath.ModConfig;

import net.minecraft.client.MinecraftClient;

/** Datos recibidos del servidor que usan la pantalla y el HUD. */
public final class ClientState {
    private ClientState() {}

    public static int hits;
    public static int needed;
    /** Momento (ms del reloj local) en que termina la Zombificacion. 0 = sin efecto. */
    public static long expiresAtMillis;

    public static void onConfig(ConfigPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        ConfigData data = ModConfig.parseTransfer(payload.json());
        if (payload.open()) {
            client.setScreen(new PermadeathScreen(data));
        } else if (client.currentScreen instanceof PermadeathScreen screen) {
            screen.onServerConfig(data);
        }
    }

    public static void onInfection(InfectionPayload payload) {
        hits = payload.hits();
        needed = payload.needed();
        expiresAtMillis = payload.ticksLeft() > 0 ? System.currentTimeMillis() + payload.ticksLeft() * 50L : 0L;
    }
}
