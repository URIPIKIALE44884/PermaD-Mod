package com.permadeath.client;

import com.permadeath.PermadeathMod;
import com.permadeath.WildfireEntity;

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

/** Modelo propio del Wildfire: cabeza grande con un anillo de 12 varillas largas (silueta propia, no copiada). */
public class WildfireModel extends SinglePartEntityModel<WildfireEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(new Identifier(PermadeathMod.MOD_ID, "wildfire"), "main");
    private final ModelPart root;
    private final ModelPart[] rods = new ModelPart[12];

    public WildfireModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < 12; i++) {
            this.rods[i] = root.getChild("rod" + i);
        }
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData rootData = data.getRoot();
        rootData.addChild("head", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f, new Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 8.0f, 0.0f));
        for (int i = 0; i < 12; i++) {
            float angle = (float) (i * Math.PI / 6.0);
            float x = MathHelper.cos(angle) * 11.0f;
            float z = MathHelper.sin(angle) * 11.0f;
            rootData.addChild("rod" + i, ModelPartBuilder.create().uv(32 + (i % 6) * 5, (i / 6) * 32)
                    .cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 28.0f, 3.0f, new Dilation(0.0f)),
                    ModelTransform.pivot(x, 2.0f, z));
        }
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(WildfireEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (int i = 0; i < this.rods.length; i++) {
            this.rods[i].pitch = 0.3f * MathHelper.sin(animationProgress * 0.2f + i * 0.7f);
        }
    }
}
