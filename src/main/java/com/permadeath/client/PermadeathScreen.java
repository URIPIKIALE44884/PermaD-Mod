package com.permadeath.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import com.permadeath.ConfigData;
import com.permadeath.GlobalSettings;
import com.permadeath.MobKind;
import com.permadeath.ModItems;
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
import net.minecraft.client.gui.widget.TextFieldWidget;
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
 * - Mobs: elegir mob (huevos de spawn), modelo 3D girando a la izquierda y opciones a la derecha.
 *   Armadura: 4 piezas con sus materiales (clic der. = activar/desactivar, clic central = probabilidad).
 * - Dificultad: infeccion zombi.
 * - Oleada: cantidad de cada mob, generar y cancelar.
 */
public class PermadeathScreen extends Screen {
    private static final Gson GSON = new Gson();
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 250;

    private static final String[] MOB_NAMES = { "Zombie", "Husk", "Ahogado", "Aldeano zombi", "Esqueleto",
            "Stray", "Esqueleto Wither", "Araña", "Araña de cueva", "Creeper", "Iceologer", "Ilusioner" };
    private static final Item[] EGGS = { Items.ZOMBIE_SPAWN_EGG, Items.HUSK_SPAWN_EGG, Items.DROWNED_SPAWN_EGG,
            Items.ZOMBIE_VILLAGER_SPAWN_EGG, Items.SKELETON_SPAWN_EGG, Items.STRAY_SPAWN_EGG,
            Items.WITHER_SKELETON_SPAWN_EGG, Items.SPIDER_SPAWN_EGG, Items.CAVE_SPIDER_SPAWN_EGG,
            Items.CREEPER_SPAWN_EGG, ModItems.ICEOLOGER_SPAWN_EGG, Items.BOW };
    private static final String[] EFFECT_KEYS = { "speed", "strength", "resistance", "regeneration",
            "fire_resistance", "invisibility", "jump_boost" };
    private static final String[] EFFECT_NAMES = { "Velocidad", "Fuerza", "Resistencia", "Regeneración",
            "Res. al fuego", "Invisibilidad", "Salto" };
    private static final String[] LEVEL_NAMES = { "Off", "I", "II", "III" };
    private static final String[] PIECE_NAMES = { "Casco", "Peto", "Pantalones", "Botas" };
    private static final String[] MATERIAL_NAMES = { "Cuero", "Malla", "Oro", "Hierro", "Diamante", "Netherita" };
    private static final String[] DIM_IDS = { "overworld", "nether", "end" };
    private static final String[] DIM_NAMES = { "Overworld", "Nether", "End" };

    private enum Tab { MOBS, DIFICULTAD, OLEADA, ILLAGERS }

    private enum Section { GENERAL, ARMADURA, EFECTOS, DIMENSIONES }

    private ConfigData data;
    private Tab tab = Tab.MOBS;
    private Section section = Section.GENERAL;
    private MobKind selected = MobKind.ZOMBIE;
    private boolean pendingRebuild = false;
    private int left;
    private int top;
    private LivingEntity preview;

    // armadura
    private int armorSlot = 0;
    private int armorSelectedMaterial = -1;
    private TextFieldWidget editField;
    private int editMaterial = -1;

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
        editField = null;
        editMaterial = -1;

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
        addDrawableChild(ButtonWidget.builder(Text.literal(tab == Tab.ILLAGERS ? "[Illagers]" : "Illagers"), b -> {
            tab = Tab.ILLAGERS;
            clearAndInit();
        }).dimensions(left + 214, top + 6, 52, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(left + PANEL_W - 62, top + 6, 56, 18).build());

        if (tab == Tab.MOBS) {
            refreshPreview();
            initMobs();
        } else if (tab == Tab.DIFICULTAD) {
            initDifficulty();
        } else if (tab == Tab.OLEADA) {
            initWave();
        } else {
            initIllagers();
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
                // campo para escribir la probabilidad (clic central sobre un material)
                editField = new TextFieldWidget(this.textRenderer, optX + 158, y0 + 4, 52, 16, Text.empty());
                editField.setMaxLength(3);
                editField.setTextPredicate(text -> text.matches("[0-9]{0,3}"));
                editField.setVisible(false);
                addDrawableChild(editField);

                addSlider(optX, y0 + 90, optW, "Encantadas (Protección I-IV)", "%", 0, 100, s.armorEnchantChance,
                        v -> s.armorEnchantChance = v, this::sendMob);
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
        addSlider(x, top + 104, w, "Reinicio del contador sin golpes (segundos)", "", 30, 600, g.infectionWindowSeconds,
                v -> g.infectionWindowSeconds = v, this::sendGlobal);
        addSlider(x, top + 126, w, "Duración del efecto (segundos)", "", 60, 900, g.infectionDurationSeconds,
                v -> g.infectionDurationSeconds = v, this::sendGlobal);
    }

    private static String infectionText(GlobalSettings g) {
        return "Activar infección: " + (g.infectionEnabled ? "Sí" : "No");
    }

    private void initWave() {
        GlobalSettings g = data.global;
        int x = left + 12;
        int w = PANEL_W - 24;

        addSlider(x, top + 50, w, "Zombies", "", 0, 60, g.waveZombieCount, v -> g.waveZombieCount = v, this::sendGlobal);
        addSlider(x, top + 72, w, "Esqueletos", "", 0, 60, g.waveSkeletonCount, v -> g.waveSkeletonCount = v, this::sendGlobal);
        addSlider(x, top + 94, w, "Arañas (invisibles y rápidas)", "", 0, 30, g.waveSpiderCount, v -> g.waveSpiderCount = v, this::sendGlobal);
        addSlider(x, top + 116, w, "Creepers (sin romper bloques)", "", 0, 30, g.waveCreeperCount, v -> g.waveCreeperCount = v, this::sendGlobal);
        addSlider(x, top + 138, w, "Esqueletos Wither", "", 0, 10, g.waveWitherCount, v -> g.waveWitherCount = v, this::sendGlobal);

        addDrawableChild(ButtonWidget.builder(Text.literal("Generar oleada (todos los jugadores conectados)"),
                b -> ClientPlayNetworking.send(new WaveActionPayload("start")))
                .dimensions(x, top + 166, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancelar oleadas"),
                b -> ClientPlayNetworking.send(new WaveActionPayload("cancel")))
                .dimensions(x, top + 190, w, 20).build());
    }

    /** Opciones de Iceologer e Ilusioner, separadas de las de mobs y oleadas. */
    private void initIllagers() {
        GlobalSettings g = data.global;
        int x = left + 12;
        int w = PANEL_W - 24;

        addSlider(x, top + 56, w, "Iceologer: iglús naturales (% por chunk nuevo)", "%", 0, 20, g.iglooChance,
                v -> g.iglooChance = v, this::sendGlobal);
        addSlider(x, top + 80, w, "Ilusioner: generación natural (% por chunk nuevo)", "%", 0, 20, g.illusionerNaturalChance,
                v -> g.illusionerNaturalChance = v, this::sendGlobal);
        addSlider(x, top + 118, w, "Iceologer: aparición en raids (% por oleada)", "%", 0, 100, g.iceologerRaidChance,
                v -> g.iceologerRaidChance = v, this::sendGlobal);
        addSlider(x, top + 142, w, "Ilusioner: aparición en raids (% por oleada)", "%", 0, 100, g.illusionerRaidChance,
                v -> g.illusionerRaidChance = v, this::sendGlobal);
    }

    private void drawIllagerTexts(DrawContext context) {
        drawText(context, "Generación natural", left + 12, top + 38, 0xFF202020);
        drawText(context, "Raids", left + 12, top + 104, 0xFF202020);
        String[] lines = {
                "Iglús: laderas nevadas y picos helados. Ilusioner: bosque oscuro.",
                "Solo cuenta para chunks NUEVOS; los ya generados no cambian.",
                "Raids: cada oleada de una raid tira el dado de cada uno por separado.",
                "Los efectos y dimensiones de estos mobs se editan en la pestaña Mobs." };
        int y = top + 176;
        for (String line : lines) {
            drawText(context, line, left + 12, y, 0xFF404040);
            y += 12;
        }
    }

    // ------------------------------------------------------------ red
    private void sendMob() {
        MobSettings s = data.mobs.get(selected.id);
        ClientPlayNetworking.send(new UpdatePayload(selected.id, GSON.toJson(s)));
    }

    private void sendGlobal() {
        ClientPlayNetworking.send(new UpdatePayload("global", GSON.toJson(data.global)));
    }

    // ------------------------------------------------------------ armadura: datos
    private boolean armorVisible() {
        return tab == Tab.MOBS && section == Section.ARMADURA
                && (selected.family == MobKind.Family.ZOMBIE || selected.family == MobKind.Family.SKELETON);
    }

    private int[] armorArray() {
        MobSettings s = data.mobs.get(selected.id);
        int[] values = s.armor.get(MobShaper.ARMOR_KEYS[armorSlot]);
        if (values == null || values.length < 6) {
            values = new int[] { -1, -1, -1, -1, -1, -1 };
            s.armor.put(MobShaper.ARMOR_KEYS[armorSlot], values);
        }
        return values;
    }

    private int sumOthers(int[] values, int except) {
        int sum = 0;
        for (int i = 0; i < values.length; i++) {
            if (i != except && values[i] > 0) {
                sum += values[i];
            }
        }
        return sum;
    }

    /** Cambia la probabilidad de un material. La suma de la pieza nunca pasa de 100%. */
    private void setMaterial(int material, int value) {
        int[] values = armorArray();
        if (values[material] < 0) {
            return;
        }
        values[material] = Math.max(0, Math.min(value, 100 - sumOthers(values, material)));
        sendMob();
        refreshPreview();
    }

    /** Clic derecho: activa o desactiva (X) un material. */
    private void toggleMaterial(int material) {
        int[] values = armorArray();
        if (values[material] >= 0) {
            values[material] = -1;
        } else {
            values[material] = Math.min(10, 100 - sumOthers(values, material));
        }
        sendMob();
        refreshPreview();
    }

    private void startEdit(int material) {
        int[] values = armorArray();
        if (editField == null || values[material] < 0) {
            return;
        }
        editMaterial = material;
        armorSelectedMaterial = material;
        editField.setText(String.valueOf(values[material]));
        editField.setVisible(true);
        this.setFocused(editField);
    }

    private void commitEdit() {
        if (editMaterial < 0 || editField == null) {
            return;
        }
        int value = 0;
        try {
            value = Integer.parseInt(editField.getText());
        } catch (NumberFormatException ignored) {
            // vacio: queda en 0
        }
        setMaterial(editMaterial, value);
        cancelEdit();
    }

    private void cancelEdit() {
        editMaterial = -1;
        if (editField != null) {
            editField.setVisible(false);
        }
        this.setFocused(null);
    }

    private int socketX(int index) {
        return left + 98 + index * 30;
    }

    private int socketY() {
        return top + 78;
    }

    private int materialX(int index) {
        return left + 98 + index * 37;
    }

    private int materialY() {
        return top + 108;
    }

    private int socketAt(double mouseX, double mouseY) {
        for (int i = 0; i < 4; i++) {
            if (mouseX >= socketX(i) && mouseX < socketX(i) + 24 && mouseY >= socketY() && mouseY < socketY() + 24) {
                return i;
            }
        }
        return -1;
    }

    private int materialAt(double mouseX, double mouseY) {
        for (int i = 0; i < 6; i++) {
            if (mouseX >= materialX(i) && mouseX < materialX(i) + 32 && mouseY >= materialY() && mouseY < materialY() + 32) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------ dibujo
    /**
     * Vacio a proposito: segun la version, Screen.render() vuelve a dibujar el fondo (oscurecido/difuminado)
     * ENCIMA de lo que ya dibujamos. Oscurecemos el mundo nosotros en render().
     */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
        drawPanel(context);
        if (tab == Tab.MOBS) {
            drawMobsArea(context);
        } else if (tab == Tab.DIFICULTAD) {
            drawDifficultyTexts(context);
        } else if (tab == Tab.OLEADA) {
            drawWaveTexts(context);
        } else {
            drawIllagerTexts(context);
        }
        super.render(context, mouseX, mouseY, delta);

        if (tab == Tab.MOBS) {
            drawEggs(context);
            if (preview != null) {
                drawEntity(context, left + 49, top + PANEL_H - 22, 48, preview);
            }
            if (armorVisible()) {
                drawArmorUi(context);
            }
            drawEggTooltip(context, mouseX, mouseY);
            if (armorVisible()) {
                drawArmorTooltip(context, mouseX, mouseY);
            }
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

    /** Los huevos se dibujan despues de los widgets para que queden nitidos. */
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

    private void drawText(DrawContext context, String text, int x, int y, int color) {
        context.drawText(this.textRenderer, Text.literal(text), x, y, color, false);
    }

    private void drawCentered(DrawContext context, String text, int centerX, int y, int color) {
        int width = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, Text.literal(text), centerX - width / 2, y, color, false);
    }

    /** Sockets de pieza (vacios) y, debajo, los materiales de la pieza elegida. */
    private void drawArmorUi(DrawContext context) {
        int optX = left + 98;
        int y0 = top + 78;
        int[] values = armorArray();
        MatrixStack matrices = context.getMatrices();

        // sockets vacios: la pieza se ve "fantasma"
        for (int i = 0; i < 4; i++) {
            int x = socketX(i);
            boolean isSelected = i == armorSlot;
            context.fill(x - 1, y0 - 1, x + 25, y0 + 25, isSelected ? 0xFF2E7D32 : 0xFF8B8B8B);
            context.fill(x, y0, x + 24, y0 + 24, 0xFF373737);
            context.drawItem(new ItemStack(MobShaper.ARMOR_ITEMS[i][3]), x + 4, y0 + 4);
            matrices.push();
            matrices.translate(0.0, 0.0, 200.0);
            context.fill(x, y0, x + 24, y0 + 24, 0xB0373737);
            matrices.pop();
        }
        if (editMaterial >= 0) {
            drawText(context, "Prob. %:", optX + 118, y0 + 8, 0xFF202020);
        }

        // materiales de la pieza elegida
        for (int j = 0; j < 6; j++) {
            int x = materialX(j);
            int y = materialY();
            int value = values[j];
            boolean isSelected = j == armorSelectedMaterial;
            context.fill(x - 1, y - 1, x + 33, y + 33, isSelected ? 0xFFFFFF55 : (value >= 0 ? 0xFF8B8B8B : 0xFF7A3A3A));
            context.fill(x, y, x + 32, y + 32, 0xFF373737);

            matrices.push();
            matrices.translate((double) x, (double) y, 0.0);
            matrices.scale(2.0f, 2.0f, 1.0f);
            context.drawItem(new ItemStack(MobShaper.ARMOR_ITEMS[armorSlot][j]), 0, 0);
            matrices.pop();

            if (value < 0) {
                matrices.push();
                matrices.translate(0.0, 0.0, 200.0);
                context.fill(x, y, x + 32, y + 32, 0xB0000000);
                drawCentered(context, "X", x + 16, y + 12, 0xFFFF5555);
                matrices.pop();
            }
            drawCentered(context, value < 0 ? "X" : value + "%", x + 16, y + 36, 0xFF202020);
        }

        int total = 0;
        boolean configured = false;
        for (int v : values) {
            if (v > 0) {
                total += v;
            }
            if (v >= 0) {
                configured = true;
            }
        }
        String summary = configured
                ? PIECE_NAMES[armorSlot] + ": total " + total + "%  -  sin pieza " + (100 - total) + "%"
                : PIECE_NAMES[armorSlot] + ": sin configurar (equipo vanilla)";
        drawText(context, summary, optX, y0 + 78, 0xFF202020);

        drawText(context, "Prot. I 50% - II 30% - III 15% - IV 5% (de las encantadas)", optX, y0 + 112, 0xFF404040);
        drawText(context, "Clic der.: activar/desactivar (X)  -  Rueda: +/-5%", optX, y0 + 124, 0xFF404040);
        drawText(context, "Clic central: escribir la probabilidad (Enter)", optX, y0 + 134, 0xFF404040);
    }

    private void drawArmorTooltip(DrawContext context, int mouseX, int mouseY) {
        int material = materialAt(mouseX, mouseY);
        if (material >= 0) {
            int value = armorArray()[material];
            String text = MATERIAL_NAMES[material] + ": " + (value < 0 ? "desactivado" : value + "%");
            context.drawTooltip(this.textRenderer, Text.literal(text), mouseX, mouseY);
            return;
        }
        int socket = socketAt(mouseX, mouseY);
        if (socket >= 0) {
            context.drawTooltip(this.textRenderer, Text.literal(PIECE_NAMES[socket]), mouseX, mouseY);
        }
    }

    private void drawDifficultyTexts(DrawContext context) {
        GlobalSettings g = data.global;
        drawText(context, "Zombies", left + 12, top + 38, 0xFF202020);
        int y = top + 156;
        String[] lines = {
                "Cada golpe directo de zombie suma al contador; con " + g.infectionHits + " golpes te infectás.",
                "Si pasan " + g.infectionWindowSeconds + " s sin recibir un golpe de zombie, el contador vuelve a 0.",
                "Zombificación durante " + g.infectionDurationSeconds + " s: sin regenerar vida; al llegar a 0 morís.",
                "Morir infectado: -2 corazones permanentes (mínimo 3) y aparece tu zombie.",
                "Cura: Antídoto (poción de curación o regeneración + carne podrida)." };
        for (String line : lines) {
            drawText(context, line, left + 12, y, 0xFF404040);
            y += 12;
        }
    }

    private void drawWaveTexts(DrawContext context) {
        drawText(context, "Oleada: cantidad de cada mob (0 = no aparece)", left + 12, top + 36, 0xFF202020);
        drawText(context, "Llega en 3 min, en tandas a 10-15 bloques; si sobrevivís 5 min, desaparecen.", left + 12, top + 218, 0xFF404040);
        drawText(context, "Persiguen solo a su jugador. Durante la oleada la infección necesita 25 golpes.", left + 12, top + 230, 0xFF404040);
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
            // en la vista previa se muestra, por pieza, el material con mas probabilidad
            for (int slot = 0; slot < MobShaper.ARMOR_KEYS.length; slot++) {
                int[] values = s.armor.get(MobShaper.ARMOR_KEYS[slot]);
                if (values == null) {
                    continue;
                }
                int best = -1;
                int bestValue = 0;
                for (int m = 0; m < values.length; m++) {
                    if (values[m] > bestValue) {
                        bestValue = values[m];
                        best = m;
                    }
                }
                if (best >= 0) {
                    living.equipStack(MobShaper.ARMOR_SLOTS[slot], new ItemStack(MobShaper.ARMOR_ITEMS[slot][best]));
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

    // ------------------------------------------------------------ mouse y teclado
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

        if (armorVisible()) {
            int socket = socketAt(mouseX, mouseY);
            if (socket >= 0 && button == 0) {
                armorSlot = socket;
                armorSelectedMaterial = -1;
                cancelEdit();
                return true;
            }
            int material = materialAt(mouseX, mouseY);
            if (material >= 0) {
                if (button == 0) {
                    armorSelectedMaterial = material;
                } else if (button == 1) {
                    cancelEdit();
                    toggleMaterial(material);
                } else if (button == 2) {
                    startEdit(material);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (armorVisible()) {
            int material = materialAt(mouseX, mouseY);
            if (material >= 0) {
                int[] values = armorArray();
                if (values[material] >= 0) {
                    setMaterial(material, values[material] + (verticalAmount > 0 ? 5 : -5));
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editMaterial >= 0) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitEdit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelEdit();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
