package com.permadeath.client;

import com.permadeath.IceologerEntity;
import com.permadeath.PermadeathMod;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.util.Identifier;

/** Modelo de illager de vanilla con la textura propia del Iceologer. */
public class IceologerRenderer extends IllagerEntityRenderer<IceologerEntity> {
    private static final Identifier TEXTURE = new Identifier(PermadeathMod.MOD_ID, "textures/entity/iceologer.png");

    public IceologerRenderer(EntityRendererFactory.Context context) {
        super(context, new IllagerEntityModel<>(context.getPart(EntityModelLayers.VINDICATOR)), 0.5f);
    }

    @Override
    public Identifier getTexture(IceologerEntity entity) {
        return TEXTURE;
    }
}
