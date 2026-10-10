package com.permadeath.client;

import com.permadeath.BlazeFatuoEntity;
import com.permadeath.PermadeathMod;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Modelo propio del Blaze Fatuo: cabeza + 8 varillas flotantes, igual de simple que el Blaze vanilla. */
public class BlazeFatuoModel extends SinglePartEntityModel<BlazeFatuoEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(new Identifier(PermadeathMod.MOD_ID, "blaze_fatuo"), "main");
    private final ModelPart root;
    private final ModelPart[] rods = new ModelPart[8];

    public BlazeFatuoModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < 8; i++) {
            this.rods[i] = root.getChild("rod" + i);
        }
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData rootData = data.getRoot();
        rootData.addChild("head", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 4.0f, 0.0f));
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI / 4.0);
            float x = MathHelper.cos(angle) * 6.0f;
            float z = MathHelper.sin(angle) * 6.0f;
            rootData.addChild("rod" + i, ModelPartBuilder.create().uv(32 + (i % 4) * 8, (i / 4) * 16)
                    .cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 16.0f, 2.0f, new Dilation(0.0f)),
                    ModelTransform.pivot(x, 0.0f, z));
        }
        return TexturedModelData.of(data, 64, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(BlazeFatuoEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (int i = 0; i < this.rods.length; i++) {
            this.rods[i].pitch = 0.4f * MathHelper.sin(animationProgress * 0.3f + i);
        }
    }
}
