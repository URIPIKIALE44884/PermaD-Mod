/*
 * Derivado de "Iceologer Mod" (NeoForge 1.21.4) de CoverWeb y MCreator, licencia Microsoft Reciprocal
 * License (Ms-RL). Los creditos pertenecen a Mojang. Este archivo es un port a Fabric 1.20.6 (nombres
 * Yarn) de los datos originales y se mantiene bajo la Ms-RL: ver LICENSE-MS-RL.txt y NOTICE.md.
 */
package com.permadeath.client;

import com.permadeath.IceologerEntity;
import com.permadeath.PermadeathMod;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

/** Dibuja al Iceologer con su modelo y textura originales (sombra de 0,5, como el mod NeoForge). */
public class IceologerRenderer extends MobEntityRenderer<IceologerEntity, IceologerModel> {
    private static final Identifier TEXTURE = new Identifier(PermadeathMod.MOD_ID, "textures/entity/iceologer.png");

    public IceologerRenderer(EntityRendererFactory.Context context) {
        super(context, new IceologerModel(context.getPart(IceologerModel.LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(IceologerEntity entity) {
        return TEXTURE;
    }
}
