package com.permadeath.client;

import com.permadeath.ConfigData;
import com.permadeath.ConfigPayload;
import com.permadeath.InfectionPayload;
import com.permadeath.ModConfig;

import com.permadeath.ModItems;
import com.permadeath.WavePayload;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

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

    /** Animacion de totem (como la del totem normal) pero con la textura del Totem de Memoria. */
    public static void onTotemAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        client.gameRenderer.showFloatingItem(new ItemStack(ModItems.TOTEM));
        client.particleManager.addEmitter(client.player, ParticleTypes.TOTEM_OF_UNDYING, 30);
        client.world.playSound(client.player.getX(), client.player.getY(), client.player.getZ(),
                SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
    }

    /** 0 = sin oleada, 1 = cuenta regresiva, 2 = oleada activa. */
    public static int waveMode;
    public static int waveRemaining;
    public static long waveExpiresAtMillis;

    public static void onWave(WavePayload payload) {
        waveMode = payload.mode();
        waveRemaining = payload.remaining();
        waveExpiresAtMillis = payload.mode() == 1 ? System.currentTimeMillis() + payload.seconds() * 1000L : 0L;
    }

    public static void onInfection(InfectionPayload payload) {
        hits = payload.hits();
        needed = payload.needed();
        expiresAtMillis = payload.ticksLeft() > 0 ? System.currentTimeMillis() + payload.ticksLeft() * 50L : 0L;
    }
}
