package com.permadeath.client;

import com.permadeath.BlazeFatuoEntity;
import com.permadeath.PermadeathMod;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class BlazeFatuoRenderer extends MobEntityRenderer<BlazeFatuoEntity, BlazeFatuoModel> {
    private static final Identifier TEXTURE = new Identifier(PermadeathMod.MOD_ID, "textures/entity/blaze_fatuo.png");

    public BlazeFatuoRenderer(EntityRendererFactory.Context context) {
        super(context, new BlazeFatuoModel(context.getPart(BlazeFatuoModel.LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(BlazeFatuoEntity entity) {
        return TEXTURE;
    }
}
