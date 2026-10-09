package com.permadeath;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class PermadeathMod implements ModInitializer {
    public static final String MOD_ID = "permadeath";

    @Override
    public void onInitialize() {
        // items, pestana, corazones, tótem, loot, stacks
        ModItems.init();
        ModItemGroups.init();
        HeartData.init();
        DeathHandler.init();
        ChestLoot.init();
        StackSizes.init();

        // efectos y pociones (Zombificacion, Antidoto)
        ModEffects.init();
        ModPotions.init();
        ModEntities.init();
        ModGameRules.init();
        ModEnchantments.init();
        ModWorldgen.init();
        BowMultishot.init();

        // configuracion de mobs, red y pantalla
        ModConfig.load();
        Networking.init();
        MobSpawnHandler.init();
        InfectionManager.init();
        WaveManager.init();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> PermadeathCommand.register(dispatcher));
    }
}
