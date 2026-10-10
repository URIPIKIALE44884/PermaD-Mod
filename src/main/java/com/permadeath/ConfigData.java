package com.permadeath;

import java.util.LinkedHashMap;
import java.util.Map;

/** Paquete de datos que viaja entre servidor y pantalla. */
public class ConfigData {
    public Map<String, MobSettings> mobs = new LinkedHashMap<>();
    public GlobalSettings global = new GlobalSettings();
}
