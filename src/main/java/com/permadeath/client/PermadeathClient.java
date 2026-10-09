package com.permadeath.client;

import com.permadeath.ConfigPayload;
import com.permadeath.InfectionPayload;
import com.permadeath.ModEntities;
import com.permadeath.TotemAnimationPayload;
import com.permadeath.WavePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class PermadeathClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onConfig(payload)));
        ClientPlayNetworking.registerGlobalReceiver(InfectionPayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onInfection(payload)));
        ClientPlayNetworking.registerGlobalReceiver(TotemAnimationPayload.ID,
                (payload, context) -> context.client().execute(ClientState::onTotemAnimation));
        ClientPlayNetworking.registerGlobalReceiver(WavePayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onWave(payload)));
        EntityRendererRegistry.register(ModEntities.INFECTED_ZOMBIE, InfectedZombieRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICEOLOGER, IceologerRenderer::new);
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> InfectionHud.render(drawContext));
    }
}
