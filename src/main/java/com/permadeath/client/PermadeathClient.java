package com.permadeath.client;

import com.permadeath.ConfigPayload;
import com.permadeath.InfectionPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class PermadeathClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onConfig(payload)));
        ClientPlayNetworking.registerGlobalReceiver(InfectionPayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onInfection(payload)));
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> InfectionHud.render(drawContext));
    }
}
