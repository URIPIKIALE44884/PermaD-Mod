package com.permadeath.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Temporizador de Zombificacion y conteo de golpes, en la esquina superior izquierda. */
public final class InfectionHud {
    private InfectionHud() {}

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }
        long now = System.currentTimeMillis();
        int x = 6;
        int y = 6;

        if (ClientState.expiresAtMillis > now) {
            long seconds = (ClientState.expiresAtMillis - now) / 1000L;
            String time = String.format("%d:%02d", seconds / 60L, seconds % 60L);
            context.drawTextWithShadow(client.textRenderer, Text.literal("Zombificación: " + time), x, y, 0xFF55FF55);
            context.drawTextWithShadow(client.textRenderer, Text.literal("Sin regeneración de vida"), x, y + 11, 0xFFFF5555);
        } else if (ClientState.needed > 0 && ClientState.hits > 0) {
            int remaining = Math.max(0, ClientState.needed - ClientState.hits);
            context.drawTextWithShadow(client.textRenderer,
                    Text.literal("Infección: " + ClientState.hits + "/" + ClientState.needed + " (faltan " + remaining + ")"),
                    x, y, 0xFFFFFF55);
        }
    }
}
