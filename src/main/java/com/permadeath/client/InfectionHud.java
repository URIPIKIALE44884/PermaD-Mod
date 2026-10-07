package com.permadeath.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

        // contador de la oleada: rojo y en negrita, sobre la barra de inventario
        if (ClientState.waveMode != 0) {
            String label;
            if (ClientState.waveMode == 1) {
                long seconds = Math.max(0L, (ClientState.waveExpiresAtMillis - now) / 1000L);
                label = String.format("OLEADA EN %d:%02d", seconds / 60L, seconds % 60L);
            } else {
                label = "OLEADA: " + ClientState.waveRemaining + " mobs";
            }
            Text text = Text.literal(label).formatted(Formatting.RED, Formatting.BOLD);
            int centerX = context.getScaledWindowWidth() / 2;
            int baseY = context.getScaledWindowHeight() - 82;
            context.drawTextWithShadow(client.textRenderer, text, centerX - client.textRenderer.getWidth(text) / 2, baseY, 0xFFFF5555);
        }
    }
}
