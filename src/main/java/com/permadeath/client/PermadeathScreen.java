package com.permadeath.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import com.permadeath.ConfigData;
import com.permadeath.GlobalSettings;
import com.permadeath.MobKind;
import com.permadeath.MobSettings;
import com.permadeath.MobShaper;
import com.permadeath.ResetPayload;
import com.permadeath.UpdatePayload;
import com.permadeath.WaveActionPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Pantalla de administracion (/function permadeath:menu o /permadeath menu).
 * Pestana "Mobs": elegir mob (huevos de spawn), modelo 3D girando a la izquierda y opciones a la derecha.
 * Pestana "Dificultad": reglas globales (infeccion zombi).
 */
public class PermadeathScreen extends Screen {
    private static final Gson GSON = new Gson();
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 250;

    private static final String[] MOB_NAMES = { "Zombie", "Husk", "Ahogado", "Aldeano zombi", "Esqueleto",
            "Stray", "Esqueleto Wither", "Araña", "Araña de cueva", "Creeper" };
    private static final Item[] EGGS = { Items.ZOMBIE_SPAWN_EGG, Items.HUSK_SPAWN_EGG, Items.DROWNED_SPAWN_EGG,
            Items.ZOMBIE_VILLAGER_SPAWN_EGG, Items.SKELETON_SPAWN_EGG, Items.STRAY_SPAWN_EGG,
            Items.WITHER_SKELETON_SPAWN_EGG, Items.SPIDER_SPAWN_EGG, Items.CAVE_SPIDER_SPAWN_EGG,
            Items.CREEPER_SPAWN_EGG };
    private static final String[] EFFECT_KEYS = { "speed", "strength", "resistance", "regeneration",
            "fire_resistance", "invisibility", "jump_boost" };
    private static final String[] EFFECT_NAMES = { "Velocidad", "Fuerza", "Resistencia", "Regeneración",
            "Res. al fuego", "Invisibilidad", "Salto" };
    private static final String[] LEVEL_NAMES = { "Off", "I", "II", "III" };
    private static final String[] ARMOR_NAMES = { "Nivel 1: Full hierro", "Nivel 2: Full diamante",
            "Nivel 3: Full netherita" };
    private static final String[] DIM_IDS = { "overworld", "nether", "end" };
    private static final String[] DIM_NAMES = { "Overworld", "Nether", "End" };

    private enum Tab { MOBS, DIFICULTAD, OLEADA }

    private enum Section { GENERAL, ARMADURA, EFECTOS, DIMENSIONES }

    private ConfigData data;
    private Tab tab = Tab.MOBS;
    private Section section = Section.GENERAL;
    private MobKind selected = MobKind.ZOMBIE;
    private boolean pendingRebuild = false;
    private int left;
    private int top;
    private LivingEntity preview;

    public PermadeathScreen(ConfigData data) {
        super(Text.literal("Permadeath"));
        this.data = data;
    }

    /** Si el servidor confirma un Default/Reset, se recargan los valores en pantalla. */
    public void onServerConfig(ConfigData newData) {
        if (pendingRebuild) {
            pendingRebuild = false;
            this.data = newData;
            clearAndInit();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ------------------------------------------------------------ construccion
    @Override
    protected void init() {
        left = (this.width - PANEL_W) / 2;
        top = Math.max(2, (this.height - PANEL_H) / 2);

        addDrawableChild(ButtonWidget.builder(Text.literal(tab == Tab.MOBS ? "[Mobs]" : "Mobs"), b -> {
            tab = Tab.MOBS;
            clearAndInit();
        }).dimensions(left + 6, top + 6, 60, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(tab == Tab.DIFICULTAD ? "[Dificultad]" : "Dificultad"), b -> {
            tab = Tab.DIFICULTAD;
            clearAndInit();
        }).dimensions(left + 70, top + 6, 76, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(tab == Tab.OLEADA ? "[Oleada]" : "Oleada"), b -> {
            tab = Tab.OLEADA;
            clearAndInit();
        }).dimensions(left + 150, top + 6, 60, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(left + PANEL_W - 62, top + 6, 56, 18).build());

        if (tab == Tab.MOBS) {
            refreshPreview();
            initMobs();
        } else if (tab == Tab.DIFICULTAD) {
            initDifficulty();
        } else {
            initWave();
        }
    }

    private List<Section> sectionsFor(MobKind kind) {
        List<Section> list = new ArrayList<>();
        list.add(Section.GENERAL);
        if (kind.family == MobKind.Family.ZOMBIE || kind.family == MobKind.Family.SKELETON) {
            list.add(Section.ARMADURA);
        }
        list.add(Section.EFECTOS);
        list.add(Section.DIMENSIONES);
        return list;
    }

    private static String sectionName(Section s) {
        return switch (s) {
            case GENERAL -> "General";
            case ARMADURA -> "Armadura";
            case EFECTOS -> "Efectos";
            case DIMENSIONES -> "Dimensiones";
        };
    }

    private void addSlider(int x, int y, int w, String label, String suffix, int min, int max, int value,
            IntConsumer onChange, Runnable onRelease) {
        addDrawableChild(new IntSlider(x, y, w, 18, label, suffix, min, max, value, onChange, onRelease));
    }

    private void initMobs() {
        MobSettings s = data.mobs.get(selected.id);
        List<Section> sections = sectionsFor(selected);
        if (!sections.contains(section)) {
            section = Section.GENERAL;
        }

        int optX = left + 98;
        int optW = PANEL_W - 104;
        int y0 = top + 78;

        int count = sections.size();
        int gap = 4;
        int buttonWidth = (optW - gap * (count - 1)) / count;
        for (int i = 0; i < count; i++) {
            Section sec = sections.get(i);
            String label = sectionName(sec);
            addDrawableChild(ButtonWidget.builder(Text.literal(sec == section ? "[" + label + "]" : label), b -> {
                section = sec;
                clearAndInit();
            }).dimensions(optX + i * (buttonWidth + gap), top + 52, buttonWidth, 18).build());
        }

        switch (section) {
            case GENERAL -> {
                int y = y0;
                addSlider(optX, y, optW, "Probabilidad general", "%", 0, 100, s.chance,
                        v -> s.chance = v, this::sendMob);
                y += 22;
                if (selected.family == MobKind.Family.ZOMBIE || selected.family == MobKind.Family.SKELETON) {
                    String special = selected.family == MobKind.Family.ZOMBIE ? "Zombies con arco" : "Escudo + espada de piedra";
                    addSlider(optX, y, optW, special, "%", 0, 100, s.specialChance,
                            v -> s.specialChance = v, this::sendMob);
                    y += 22;
                    addDrawableChild(ButtonWidget.builder(Text.literal(dropText(s)), b -> {
                        s.dropEquipment = !s.dropEquipment;
                        b.setMessage(Text.literal(dropText(s)));
                        sendMob();
                    }).dimensions(optX, y, optW, 18).build());
                } else if (selected.family == MobKind.Family.CREEPER) {
                    addSlider(optX, y, optW, "Radio de explosión (3 = vanilla)", "", 1, 12, s.creeperRadius,
                            v -> s.creeperRadius = v, this::sendMob);
                }
            }
            case ARMADURA -> {
                for (int i = 0; i < 3; i++) {
                    final int idx = i;
                    addSlider(optX, y0 + i * 22, optW, ARMOR_NAMES[i] + "  P", "%", 0, 100, s.armorChance[i],
                            v -> s.armorChance[idx] = v,
                            () -> {
                                sendMob();
                                refreshPreview();
                            });
                }
            }
            case EFECTOS -> {
                for (int i = 0; i < EFFECT_KEYS.length; i++) {
                    final String key = EFFECT_KEYS[i];
                    final String name = EFFECT_NAMES[i];
                    int y = y0 + i * 20;
                    int[] current = s.effects.computeIfAbsent(key, k -> new int[] { 0, 100 });
                    addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + LEVEL_NAMES[current[0]]), b -> {
                        current[0] = (current[0] + 1) % 4;
                        b.setMessage(Text.literal(name + ": " + LEVEL_NAMES[current[0]]));
                        sendMob();
                    }).dimensions(optX, y, 112, 18).build());
                    addSlider(optX + 116, y, optW - 116, "Prob", "%", 0, 100, current[1],
                            v -> current[1] = v, this::sendMob);
                }
            }
            case DIMENSIONES -> {
                for (int i = 0; i < DIM_IDS.length; i++) {
                    final String dim = DIM_IDS[i];
                    final String name = DIM_NAMES[i];
                    addDrawableChild(ButtonWidget.builder(dimText(s, dim, name), b -> {
                        if (!s.allowedDims.remove(dim)) {
                            s.allowedDims.add(dim);
                        }
                        b.setMessage(dimText(s, dim, name));
                        sendMob();
                    }).dimensions(optX, y0 + i * 22, optW, 18).build());
                }
            }
        }

        int footerY = top + PANEL_H - 26;
        addDrawableChild(ButtonWidget.builder(Text.literal("Default"), b -> {
            pendingRebuild = true;
            ClientPlayNetworking.send(new ResetPayload(selected.id));
        }).dimensions(optX, footerY, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset (todos)"), b -> {
            pendingRebuild = true;
            ClientPlayNetworking.send(new ResetPayload("*"));
        }).dimensions(optX + 74, footerY, 100, 20).build());
    }

    private static String dropText(MobSettings s) {
        return "Drop del equipo: " + (s.dropEquipment ? "Sí" : "No");
    }

    private static Text dimText(MobSettings s, String dim, String name) {
        return Text.literal("[" + (s.allowedDims.contains(dim) ? "x" : " ") + "] " + name);
    }

    private void initDifficulty() {
        GlobalSettings g = data.global;
        int x = left + 12;
        int w = PANEL_W - 24;

        addDrawableChild(ButtonWidget.builder(Text.literal(infectionText(g)), b -> {
            g.infectionEnabled = !g.infectionEnabled;
            b.setMessage(Text.literal(infectionText(g)));
            sendGlobal();
        }).dimensions(x, top + 56, w, 20).build());
        addSlider(x, top + 82, w, "Golpes necesarios", "", 1, 30, g.infectionHits,
                v -> g.infectionHits = v, this::sendGlobal);
        addSlider(x, top + 104, w, "Ventana de tiempo (segundos)", "", 30, 600, g.infectionWindowSeconds,
                v -> g.infectionWindowSeconds = v, this::sendGlobal);
        addSlider(x, top + 126, w, "Duración del efecto (segundos)", "", 60, 900, g.infectionDurationSeconds,
                v -> g.infectionDurationSeconds = v, this::sendGlobal);
    }

    private void initWave() {
        GlobalSettings g = data.global;
        int x = left + 12;
        int w = PANEL_W - 24;

        addDrawableChild(ButtonWidget.builder(Text.literal(waveToggleText("Arañas (5, invisibles y rápidas)", g.waveSpiders)), b -> {
            g.waveSpiders = !g.waveSpiders;
            b.setMessage(Text.literal(waveToggleText("Arañas (5, invisibles y rápidas)", g.waveSpiders)));
            sendGlobal();
        }).dimensions(x, top + 56, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(waveToggleText("Creepers (5, sin romper bloques)", g.waveCreepers)), b -> {
            g.waveCreepers = !g.waveCreepers;
            b.setMessage(Text.literal(waveToggleText("Creepers (5, sin romper bloques)", g.waveCreepers)));
            sendGlobal();
        }).dimensions(x, top + 80, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(waveToggleText("Esqueletos Wither (2)", g.waveWither)), b -> {
            g.waveWither = !g.waveWither;
            b.setMessage(Text.literal(waveToggleText("Esqueletos Wither (2)", g.waveWither)));
            sendGlobal();
        }).dimensions(x, top + 104, w, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Generar oleada (todos los jugadores conectados)"),
                b -> ClientPlayNetworking.send(new WaveActionPayload("start")))
                .dimensions(x, top + 136, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancelar oleadas"),
                b -> ClientPlayNetworking.send(new WaveActionPayload("cancel")))
                .dimensions(x, top + 160, w, 20).build());
    }

    private static String waveToggleText(String label, boolean on) {
        return label + ": " + (on ? "Sí" : "No");
    }

    private void drawWaveTexts(DrawContext context) {
        context.drawText(this.textRenderer, Text.literal("Oleada"), left + 12, top + 38, 0xFF202020, false);
        String[] lines = {
                "Siempre: 10 zombies y 12 esqueletos (con tu configuración de spawn).",
                "Llega en 3 minutos y aparece en tandas, a 10-15 bloques del jugador.",
                "Persiguen solo a su jugador, ponen andamios y rompen bloques.",
                "Durante la oleada la infección necesita 25 golpes." };
        int y = top + 190;
        for (String line : lines) {
            context.drawText(this.textRenderer, Text.literal(line), left + 12, y, 0xFF404040, false);
            y += 12;
        }
    }

    private static String infectionText(GlobalSettings g) {
        return "Activar infección: " + (g.infectionEnabled ? "Sí" : "No");
    }

    // ------------------------------------------------------------ red
    private void sendMob() {
        MobSettings s = data.mobs.get(selected.id);
        ClientPlayNetworking.send(new UpdatePayload(selected.id, GSON.toJson(s)));
    }

    private void sendGlobal() {
        ClientPlayNetworking.send(new UpdatePayload("global", GSON.toJson(data.global)));
    }

    // ------------------------------------------------------------ dibujo
    /**
     * Vacio a proposito: segun la version, Screen.render() vuelve a dibujar el fondo (oscurecido/difuminado)
     * ENCIMA de lo que ya dibujamos, y por eso el panel y los mobs se veian borrosos.
     */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // oscurecemos el mundo nosotros (sin el fondo del juego, que difuminaba el panel y los mobs)
        context.fill(0, 0, this.width, this.height, 0x90000000);
        drawPanel(context);
        if (tab == Tab.MOBS) {
            drawMobsArea(context);
        } else if (tab == Tab.DIFICULTAD) {
            drawDifficultyTexts(context);
        } else {
            drawWaveTexts(context);
        }
        super.render(context, mouseX, mouseY, delta);
        if (tab == Tab.MOBS) {
            drawEggs(context);
            if (preview != null) {
                drawEntity(context, left + 49, top + PANEL_H - 22, 48, preview);
            }
            drawEggTooltip(context, mouseX, mouseY);
        }
    }

    private void drawPanel(DrawContext context) {
        int x1 = left;
        int y1 = top;
        int x2 = left + PANEL_W;
        int y2 = top + PANEL_H;
        context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        context.fill(x1, y1, x2, y2, 0xFFC6C6C6);
        context.fill(x1, y1, x2, y1 + 1, 0xFFFFFFFF);
        context.fill(x1, y1, x1 + 1, y2, 0xFFFFFFFF);
        context.fill(x1, y2 - 1, x2, y2, 0xFF555555);
        context.fill(x2 - 1, y1, x2, y2, 0xFF555555);
    }

    private int eggX(int index) {
        return left + 8 + index * 22;
    }

    private void drawMobsArea(DrawContext context) {
        context.fill(left + 6, top + 52, left + 92, top + PANEL_H - 8, 0xFF000000);
        context.fill(left + 7, top + 53, left + 91, top + PANEL_H - 9, 0xFF1B1B1B);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(MOB_NAMES[selected.ordinal()]),
                left + 49, top + 57, 0xFFFFFFFF);
    }

    private void drawEggs(DrawContext context) {
        MobKind[] kinds = MobKind.values();
        for (int i = 0; i < kinds.length; i++) {
            int x = eggX(i);
            int y = top + 28;
            boolean isSelected = kinds[i] == selected;
            context.fill(x - 1, y - 1, x + 19, y + 19, isSelected ? 0xFF2E7D32 : 0xFF8B8B8B);
            context.fill(x, y, x + 18, y + 18, 0xFF373737);
            context.drawItem(new ItemStack(EGGS[i]), x + 1, y + 1);
        }
    }

    private void drawEggTooltip(DrawContext context, int mouseX, int mouseY) {
        for (int i = 0; i < MobKind.values().length; i++) {
            if (mouseX >= eggX(i) && mouseX < eggX(i) + 18 && mouseY >= top + 28 && mouseY < top + 46) {
                context.drawTooltip(this.textRenderer, Text.literal(MOB_NAMES[i]), mouseX, mouseY);
            }
        }
    }

    private void drawDifficultyTexts(DrawContext context) {
        GlobalSettings g = data.global;
        context.drawText(this.textRenderer, Text.literal("Zombies"), left + 12, top + 38, 0xFF202020, false);
        int y = top + 156;
        String[] lines = {
                "Si un jugador recibe " + g.infectionHits + " golpes de zombies en " + g.infectionWindowSeconds + " s,",
                "se infecta: Zombificación durante " + g.infectionDurationSeconds + " s.",
                "Sin regeneración de vida; al llegar a 0 el jugador muere.",
                "Morir infectado: -2 corazones permanentes (mínimo 3).",
                "Cura: Antídoto (poción de curación o regeneración + carne podrida)." };
        for (String line : lines) {
            context.drawText(this.textRenderer, Text.literal(line), left + 12, y, 0xFF404040, false);
            y += 12;
        }
    }

    // ------------------------------------------------------------ vista previa 3D
    private void refreshPreview() {
        preview = null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }
        Entity entity = selected.type.create(client.world);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        preview = living;
        MobSettings s = data.mobs.get(selected.id);
        if (s != null && (selected.family == MobKind.Family.ZOMBIE || selected.family == MobKind.Family.SKELETON)) {
            for (int level = 3; level >= 1; level--) {
                if (s.armorChance[level - 1] > 0) {
                    for (int i = 0; i < MobShaper.ARMOR_SLOTS.length; i++) {
                        living.equipStack(MobShaper.ARMOR_SLOTS[i], new ItemStack(MobShaper.ARMOR[level - 1][i]));
                    }
                    break;
                }
            }
        }
    }

    /** Dibuja el mob girando (como el modelo del spawner). x, y = posicion de los pies. */
    private void drawEntity(DrawContext context, int x, int y, int size, LivingEntity entity) {
        float angle = (System.currentTimeMillis() % 6000L) / 6000.0f * 360.0f;
        entity.bodyYaw = angle;
        entity.prevBodyYaw = angle;
        entity.headYaw = angle;
        entity.prevHeadYaw = angle;
        entity.setYaw(angle);
        entity.prevYaw = angle;
        entity.setPitch(0.0f);
        entity.prevPitch = 0.0f;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((double) x, (double) y, 50.0);
        matrices.multiplyPositionMatrix(new Matrix4f().scaling((float) size, (float) size, (float) -size));
        matrices.multiply(new Quaternionf().rotateZ((float) Math.PI));

        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        dispatcher.setRotation(new Quaternionf());
        dispatcher.setRenderShadows(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matrices,
                context.getVertexConsumers(), 0xF000F0));
        context.draw();
        dispatcher.setRenderShadows(true);
        matrices.pop();
    }

    // ------------------------------------------------------------ mouse
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == Tab.MOBS && button == 0) {
            MobKind[] kinds = MobKind.values();
            for (int i = 0; i < kinds.length; i++) {
                if (mouseX >= eggX(i) && mouseX < eggX(i) + 18 && mouseY >= top + 28 && mouseY < top + 46) {
                    selected = kinds[i];
                    clearAndInit();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
