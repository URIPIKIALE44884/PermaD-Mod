package com.permadeath;

import net.fabricmc.api.ModInitializer;

public class PermadeathMod implements ModInitializer {
    public static final String MOD_ID = "permadeath";

    @Override
    public void onInitialize() {
        ModItems.init();
        ModItemGroups.init();
        HeartData.init();
        DeathHandler.init();
        ChestLoot.init();
        StackSizes.init();
    }
}
