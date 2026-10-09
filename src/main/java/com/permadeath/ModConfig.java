package com.permadeath;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

/** Guarda y carga config/permadeath/mobs.json y global.json. */
public final class ModConfig {
    private ModConfig() {}

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson COMPACT = new Gson();
    private static final Type MOBS_TYPE = new TypeToken<LinkedHashMap<String, MobSettings>>() {}.getType();
    private static final Map<String, MobSettings> SETTINGS = new LinkedHashMap<>();
    private static GlobalSettings global = new GlobalSettings();

    private static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("permadeath");
    }

    // ------------------------------------------------------------ acceso
    public static synchronized MobSettings get(MobKind kind) {
        MobSettings s = SETTINGS.get(kind.id);
        if (s == null) {
            s = MobSettings.defaultFor(kind);
            SETTINGS.put(kind.id, s);
        }
        return s;
    }

    public static synchronized GlobalSettings global() {
        return global;
    }

    public static synchronized void setMob(MobKind kind, MobSettings settings) {
        settings.fix(kind);
        SETTINGS.put(kind.id, settings);
        save();
    }

    public static synchronized void setGlobal(GlobalSettings settings) {
        settings.fix();
        global = settings;
        save();
    }

    public static synchronized void resetKind(MobKind kind) {
        SETTINGS.put(kind.id, MobSettings.defaultFor(kind));
        save();
    }

    public static synchronized void resetAll() {
        SETTINGS.clear();
        for (MobKind kind : MobKind.values()) {
            SETTINGS.put(kind.id, MobSettings.defaultFor(kind));
        }
        save();
    }

    // ------------------------------------------------------------ red
    public static synchronized String toTransferJson() {
        ConfigData data = new ConfigData();
        for (MobKind kind : MobKind.values()) {
            data.mobs.put(kind.id, get(kind));
        }
        data.global = global;
        return COMPACT.toJson(data);
    }

    public static ConfigData parseTransfer(String json) {
        ConfigData data = COMPACT.fromJson(json, ConfigData.class);
        if (data == null) {
            data = new ConfigData();
        }
        for (MobKind kind : MobKind.values()) {
            MobSettings s = data.mobs.get(kind.id);
            if (s == null) {
                data.mobs.put(kind.id, MobSettings.defaultFor(kind));
            } else {
                s.fix(kind);
            }
        }
        if (data.global == null) {
            data.global = new GlobalSettings();
        }
        data.global.fix();
        return data;
    }

    public static MobSettings parseMob(String json) {
        return COMPACT.fromJson(json, MobSettings.class);
    }

    public static GlobalSettings parseGlobal(String json) {
        return COMPACT.fromJson(json, GlobalSettings.class);
    }

    // ------------------------------------------------------------ archivo
    public static synchronized void load() {
        SETTINGS.clear();
        Path mobsFile = dir().resolve("mobs.json");
        if (Files.exists(mobsFile)) {
            try {
                Map<String, MobSettings> loaded = PRETTY.fromJson(
                        Files.readString(mobsFile, StandardCharsets.UTF_8), MOBS_TYPE);
                if (loaded != null) {
                    SETTINGS.putAll(loaded);
                }
            } catch (Exception e) {
                System.err.println("[permadeath] No se pudo leer mobs.json, se usan valores por defecto: " + e);
            }
        }
        for (MobKind kind : MobKind.values()) {
            MobSettings s = SETTINGS.get(kind.id);
            if (s == null) {
                SETTINGS.put(kind.id, MobSettings.defaultFor(kind));
            } else {
                s.fix(kind);
            }
        }

        global = new GlobalSettings();
        Path globalFile = dir().resolve("global.json");
        if (Files.exists(globalFile)) {
            try {
                GlobalSettings loaded = PRETTY.fromJson(
                        Files.readString(globalFile, StandardCharsets.UTF_8), GlobalSettings.class);
                if (loaded != null) {
                    global = loaded;
                }
            } catch (Exception e) {
                System.err.println("[permadeath] No se pudo leer global.json, se usan valores por defecto: " + e);
            }
        }
        global.fix();
        save();
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(dir());
            Files.writeString(dir().resolve("mobs.json"), PRETTY.toJson(SETTINGS, MOBS_TYPE), StandardCharsets.UTF_8);
            Files.writeString(dir().resolve("global.json"), PRETTY.toJson(global), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[permadeath] No se pudo guardar la configuracion: " + e);
        }
    }
}
