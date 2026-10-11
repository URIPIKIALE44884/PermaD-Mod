package com.permadeath.client;

import com.permadeath.BlazeFatuoEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

/** El Blaze Fatuo usa deliberadamente la textura vanilla del Blaze. */
public class BlazeFatuoRenderer extends MobEntityRenderer<BlazeFatuoEntity, BlazeFatuoModel> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/blaze.png");

    public BlazeFatuoRenderer(EntityRendererFactory.Context context) {
        super(context, new BlazeFatuoModel(context.getPart(BlazeFatuoModel.LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(BlazeFatuoEntity entity) {
        return TEXTURE;
    }
}
