package com.permadeath;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

/** Reglas del Iceologer (mismos nombres y valores por defecto que en el mod NeoForge). */
public final class ModGameRules {
    private ModGameRules() {}

    /** Al tocar a un jugador, cae hielo compacto desde arriba del Iceologer. */
    public static final GameRules.Key<GameRules.BooleanRule> DROP_ICE_CHUNKS =
            GameRuleRegistry.register("dropIceChunks", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));

    /** Al caer, el Iceologer convierte el bloque que toca en hielo compacto. */
    public static final GameRules.Key<GameRules.BooleanRule> ICEOLOGER_ICE_ON_FALL =
            GameRuleRegistry.register("iceologerTurnsBlocksTouchedWhenFallingIntoIce",
                    GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));

    public static void init() {}
}
