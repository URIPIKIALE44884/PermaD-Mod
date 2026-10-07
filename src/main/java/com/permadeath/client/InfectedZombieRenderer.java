package com.permadeath.client;

import java.util.UUID;

import com.permadeath.InfectedZombieEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

/** Dibuja al zombie infectado con la skin del jugador que murio. */
public class InfectedZombieRenderer extends BipedEntityRenderer<InfectedZombieEntity, PlayerEntityModel<InfectedZombieEntity>> {
    public InfectedZombieRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(InfectedZombieEntity entity) {
        String raw = entity.getOwnerUuid();
        UUID id = new UUID(0L, 0L);
        if (!raw.isEmpty()) {
            try {
                id = UUID.fromString(raw);
            } catch (IllegalArgumentException ignored) {
                // se usa la skin por defecto
            }
            ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
            if (handler != null) {
                PlayerListEntry entry = handler.getPlayerListEntry(id);
                if (entry != null) {
                    return entry.getSkinTextures().texture();
                }
            }
        }
        return DefaultSkinHelper.getSkinTextures(id).texture();
    }
}
