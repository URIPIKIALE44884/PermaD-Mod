package com.permadeath.client;

import com.permadeath.ConfigPayload;
import com.permadeath.ClanOpenPayload;
import com.permadeath.InfectionPayload;
import com.permadeath.ModEntities;
import com.permadeath.TotemAnimationPayload;
import com.permadeath.WavePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class PermadeathClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClanOpenPayload.ID, (payload, context) -> context.client().execute(() -> context.client().setScreen(new ClanScreen())));
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onConfig(payload)));
        ClientPlayNetworking.registerGlobalReceiver(InfectionPayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onInfection(payload)));
        ClientPlayNetworking.registerGlobalReceiver(TotemAnimationPayload.ID,
                (payload, context) -> context.client().execute(ClientState::onTotemAnimation));
        ClientPlayNetworking.registerGlobalReceiver(WavePayload.ID,
                (payload, context) -> context.client().execute(() -> ClientState.onWave(payload)));
        EntityRendererRegistry.register(ModEntities.INFECTED_ZOMBIE, InfectedZombieRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(IceologerModel.LAYER, IceologerModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.ICEOLOGER, IceologerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(BlazeFatuoModel.LAYER, BlazeFatuoModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.BLAZE_FATUO, BlazeFatuoRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(WildfireModel.LAYER, WildfireModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.WILDFIRE, WildfireRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOUL_BLAZE_FIREBALL,
                context -> new FlyingItemEntityRenderer<>(context, 0.75F, true));
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> InfectionHud.render(drawContext));
    }
}