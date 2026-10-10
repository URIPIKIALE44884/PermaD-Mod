package com.permadeath;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.BlazeEntityRenderer;
import net.minecraft.client.render.entity.PillagerEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.ZombieEntityRenderer;

/**
 * Registra un renderizador para cada entidad propia. Sin este registro,
 * EntityRenderDispatcher devuelve null y el juego puede crashear al verla.
 */
public final class PermadeathModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.INFECTED_ZOMBIE, ZombieEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICEOLOGER, PillagerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.BLAZE_FATUO, BlazeEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WILDFIRE, BlazeEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOUL_BLAZE_FIREBALL, context -> new FlyingItemEntityRenderer<>(context, 0.75F, true));
    }
}
