package com.permadeath.client;

import com.permadeath.PermadeathMod;
import com.permadeath.WildfireEntity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

/** La 2da fase tiñe al Wildfire de celeste-blanco (fuego de almas) en vez de su naranja habitual. */
public class WildfireRenderer extends MobEntityRenderer<WildfireEntity, WildfireModel> {
    private static final Identifier TEXTURE = new Identifier(PermadeathMod.MOD_ID, "textures/entity/wildfire.png");

    public WildfireRenderer(EntityRendererFactory.Context context) {
        super(context, new WildfireModel(context.getPart(WildfireModel.LAYER)), 1.0f);
    }

    @Override
    public Identifier getTexture(WildfireEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(WildfireEntity entity, net.minecraft.util.math.BlockPos pos) {
        return 15;
    }

    @Override
    public void render(WildfireEntity entity, float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {
        float[] color = entity.isPhase2() ? new float[] { 0.75f, 0.95f, 1.0f } : new float[] { 1.0f, 1.0f, 1.0f };
        matrices.push();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
