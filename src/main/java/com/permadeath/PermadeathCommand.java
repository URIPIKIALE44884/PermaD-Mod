package com.permadeath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Configuracion por comandos (solo operadores). La pantalla edita exactamente estos mismos datos.
 */
public final class PermadeathCommand {
    private PermadeathCommand() {}

    private static final List<String> DIMS = List.of("overworld", "nether", "end");

    private static RequiredArgumentBuilder<ServerCommandSource, String> mobArg() {
        return CommandManager.argument("mob", StringArgumentType.word())
                .suggests((ctx, builder) -> CommandSource.suggestMatching(MobKind.ids(), builder));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("permadeath")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("list").executes(PermadeathCommand::list))
                .then(CommandManager.literal("wave")
                        .then(CommandManager.literal("start").executes(ctx -> say(ctx,
                                "Oleada iniciada para " + WaveManager.startAll(ctx.getSource().getServer()) + " jugador(es).")))
                        .then(CommandManager.literal("cancel").executes(ctx -> {
                            WaveManager.cancelAll(ctx.getSource().getServer());
                            return say(ctx, "Oleadas canceladas.");
                        })))
                .then(CommandManager.literal("menu").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        return fail(ctx, "Solo un jugador puede abrir el menu.");
                    }
                    Networking.sendConfig(player, true);
                    return 1;
                }))
                .then(CommandManager.literal("infection")
                        .then(CommandManager.argument("activar", BoolArgumentType.bool()).executes(ctx -> {
                            GlobalSettings global = ModConfig.global();
                            global.infectionEnabled = BoolArgumentType.getBool(ctx, "activar");
                            ModConfig.setGlobal(global);
                            return say(ctx, "Infeccion zombi: " + (global.infectionEnabled ? "activada" : "desactivada"));
                        })))
                .then(CommandManager.literal("show")
                        .then(mobArg().executes(ctx -> withMob(ctx, (kind, s) -> show(ctx, kind, s)))))
                .then(CommandManager.literal("default")
                        .then(mobArg().executes(ctx -> withMob(ctx, (kind, s) -> {
                            ModConfig.resetKind(kind);
                            return say(ctx, kind.id + " vuelve a su estado base.");
                        }))))
                .then(CommandManager.literal("reset").executes(ctx -> {
                    ModConfig.resetAll();
                    return say(ctx, "Todos los mobs vuelven a su estado base.");
                }))
                .then(CommandManager.literal("set").then(mobArg()
                        .then(CommandManager.literal("chance")
                                .then(CommandManager.argument("valor", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> withMob(ctx, (kind, s) -> {
                                            s.chance = IntegerArgumentType.getInteger(ctx, "valor");
                                            return saved(ctx, kind, "probabilidad general = " + s.chance + "%");
                                        }))))
                        .then(CommandManager.literal("armor")
                                .then(CommandManager.argument("pieza", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of(MobShaper.ARMOR_KEYS), builder))
                                        .then(CommandManager.argument("material", StringArgumentType.word())
                                                .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of(MobShaper.MATERIAL_KEYS), builder))
                                                .then(CommandManager.argument("prob", IntegerArgumentType.integer(-1, 100))
                                                        .executes(ctx -> withMob(ctx, (kind, s) -> setArmor(ctx, kind, s)))))))
                        .then(CommandManager.literal("enchant")
                                .then(CommandManager.argument("prob", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> withMob(ctx, (kind, s) -> {
                                            s.armorEnchantChance = IntegerArgumentType.getInteger(ctx, "prob");
                                            return saved(ctx, kind, "armaduras encantadas (Proteccion I-IV) = " + s.armorEnchantChance + "%");
                                        }))))
                        .then(CommandManager.literal("special")
                                .then(CommandManager.argument("prob", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> withMob(ctx, (kind, s) -> {
                                            s.specialChance = IntegerArgumentType.getInteger(ctx, "prob");
                                            return saved(ctx, kind, "equipo especial = " + s.specialChance + "%");
                                        }))))
                        .then(CommandManager.literal("drop")
                                .then(CommandManager.argument("valor", BoolArgumentType.bool())
                                        .executes(ctx -> withMob(ctx, (kind, s) -> {
                                            s.dropEquipment = BoolArgumentType.getBool(ctx, "valor");
                                            return saved(ctx, kind, "drop del equipo = " + s.dropEquipment);
                                        }))))
                        .then(CommandManager.literal("effect")
                                .then(CommandManager.argument("efecto", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                new ArrayList<>(MobShaper.EFFECTS.keySet()), builder))
                                        .then(CommandManager.argument("nivel", IntegerArgumentType.integer(0, 3))
                                                .then(CommandManager.argument("prob", IntegerArgumentType.integer(0, 100))
                                                        .executes(ctx -> withMob(ctx, (kind, s) -> {
                                                            String effect = StringArgumentType.getString(ctx, "efecto");
                                                            if (!MobShaper.EFFECTS.containsKey(effect)) {
                                                                return fail(ctx, "Efecto desconocido: " + effect);
                                                            }
                                                            int level = IntegerArgumentType.getInteger(ctx, "nivel");
                                                            int prob = IntegerArgumentType.getInteger(ctx, "prob");
                                                            s.effects.put(effect, new int[] { level, prob });
                                                            return saved(ctx, kind, "efecto " + effect + " nivel " + level + " (" + prob + "%)");
                                                        }))))))
                        .then(CommandManager.literal("dimension")
                                .then(CommandManager.argument("dim", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(DIMS, builder))
                                        .then(CommandManager.argument("permitir", BoolArgumentType.bool())
                                                .executes(ctx -> withMob(ctx, (kind, s) -> {
                                                    String dim = StringArgumentType.getString(ctx, "dim");
                                                    if (!DIMS.contains(dim)) {
                                                        return fail(ctx, "Dimension desconocida: " + dim);
                                                    }
                                                    if (BoolArgumentType.getBool(ctx, "permitir")) {
                                                        s.allowedDims.add(dim);
                                                    } else {
                                                        s.allowedDims.remove(dim);
                                                    }
                                                    return saved(ctx, kind, "dimensiones = " + s.allowedDims
                                                            + " (los spawns NUEVOS en otras dimensiones requieren reiniciar el servidor)");
                                                })))))
                        .then(CommandManager.literal("radius")
                                .then(CommandManager.argument("valor", IntegerArgumentType.integer(1, 12))
                                        .executes(ctx -> withMob(ctx, (kind, s) -> {
                                            if (kind.family != MobKind.Family.CREEPER) {
                                                return fail(ctx, "El radio de explosion es solo para el creeper.");
                                            }
                                            s.creeperRadius = IntegerArgumentType.getInteger(ctx, "valor");
                                            return saved(ctx, kind, "radio de explosion = " + s.creeperRadius + " (3 = vanilla)");
                                        })))))));
    }

    // ---------------------------------------------------------------- armadura
    private static int setArmor(CommandContext<ServerCommandSource> ctx, MobKind kind, MobSettings s) {
        String piece = StringArgumentType.getString(ctx, "pieza");
        String material = StringArgumentType.getString(ctx, "material");
        int prob = IntegerArgumentType.getInteger(ctx, "prob");
        int materialIndex = Arrays.asList(MobShaper.MATERIAL_KEYS).indexOf(material);
        int[] values = s.armor.get(piece);
        if (values == null || materialIndex < 0) {
            return fail(ctx, "Pieza o material desconocido. Piezas: head, chest, legs, feet. "
                    + "Materiales: leather, chainmail, gold, iron, diamond, netherite.");
        }
        int others = 0;
        for (int i = 0; i < values.length; i++) {
            if (i != materialIndex && values[i] > 0) {
                others += values[i];
            }
        }
        if (prob > 0 && others + prob > 100) {
            return fail(ctx, "La suma de los materiales de esa pieza no puede pasar de 100% (los otros suman " + others + "%).");
        }
        values[materialIndex] = prob;
        return saved(ctx, kind, piece + " / " + material + " = " + (prob < 0 ? "X (desactivado)" : prob + "%"));
    }

    private static String armorSummary(MobSettings s) {
        StringBuilder sb = new StringBuilder();
        for (String key : MobShaper.ARMOR_KEYS) {
            int[] v = s.armor.get(key);
            sb.append(key).append(" [");
            for (int i = 0; i < v.length; i++) {
                sb.append(i == 0 ? "" : " | ").append(v[i] < 0 ? "X" : String.valueOf(v[i]));
            }
            sb.append("] ");
        }
        return sb.toString().trim();
    }

    // ---------------------------------------------------------------- helpers
    private interface MobAction {
        int run(MobKind kind, MobSettings settings);
    }

    private static int withMob(CommandContext<ServerCommandSource> ctx, MobAction action) {
        String id = StringArgumentType.getString(ctx, "mob");
        MobKind kind = MobKind.byId(id);
        if (kind == null) {
            return fail(ctx, "Mob desconocido: " + id + ". Usa /permadeath list");
        }
        return action.run(kind, ModConfig.get(kind));
    }

    private static int saved(CommandContext<ServerCommandSource> ctx, MobKind kind, String message) {
        ModConfig.save();
        return say(ctx, kind.id + ": " + message);
    }

    private static int say(CommandContext<ServerCommandSource> ctx, String message) {
        ctx.getSource().sendFeedback(() -> Text.literal(message), true);
        return 1;
    }

    private static int fail(CommandContext<ServerCommandSource> ctx, String message) {
        ctx.getSource().sendError(Text.literal(message));
        return 0;
    }

    private static int list(CommandContext<ServerCommandSource> ctx) {
        return say(ctx, "Mobs: " + String.join(", ", MobKind.ids()));
    }

    private static int show(CommandContext<ServerCommandSource> ctx, MobKind kind, MobSettings s) {
        StringBuilder sb = new StringBuilder();
        sb.append(kind.id).append(": probabilidad general ").append(s.chance).append("%");
        if (kind.family == MobKind.Family.ZOMBIE || kind.family == MobKind.Family.SKELETON) {
            sb.append(" | armadura (cuero|malla|oro|hierro|diamante|netherita) ").append(armorSummary(s));
            sb.append(" | encantadas ").append(s.armorEnchantChance).append("%");
            sb.append(" | equipo especial ").append(s.specialChance).append("%");
            sb.append(" | drop ").append(s.dropEquipment);
        }
        if (kind.family == MobKind.Family.CREEPER) {
            sb.append(" | radio de explosion ").append(s.creeperRadius);
        }
        sb.append(" | dimensiones ").append(s.allowedDims);
        StringBuilder fx = new StringBuilder();
        for (Map.Entry<String, int[]> e : s.effects.entrySet()) {
            int[] v = e.getValue();
            if (v != null && v.length >= 2 && v[0] > 0) {
                fx.append(e.getKey()).append(' ').append(v[0]).append(" (").append(v[1]).append("%) ");
            }
        }
        sb.append(" | efectos: ").append(fx.length() == 0 ? "ninguno" : fx.toString().trim());
        return say(ctx, sb.toString());
    }
}
