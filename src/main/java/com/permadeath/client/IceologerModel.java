/*
 * Derivado de "Iceologer Mod" (NeoForge 1.21.4) de CoverWeb y MCreator, licencia Microsoft Reciprocal
 * License (Ms-RL). Los creditos pertenecen a Mojang. Este archivo es un port a Fabric 1.20.6 (nombres
 * Yarn) de los datos originales y se mantiene bajo la Ms-RL: ver LICENSE-MS-RL.txt y NOTICE.md.
 */
package com.permadeath.client;

import com.permadeath.IceologerEntity;
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

/**
 * Modelo original del Iceologer: cabeza con nariz y capucha, cuerpo con capa, brazos y piernas.
 * Textura de 128x64. No sigue la cabeza ni mueve las partes por su cuenta: solo reproduce las
 * animaciones originales (reposo y caminata), igual que el mod NeoForge.
 */
public class IceologerModel extends SinglePartEntityModel<IceologerEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(new Identifier(PermadeathMod.MOD_ID, "iceologer"), "main");

    private final ModelPart root;

    public IceologerModel(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData rootData = modelData.getRoot();

        ModelPartData head = rootData.addChild("head",
                ModelPartBuilder.create().uv(0, 0)
                        .cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, new Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        head.addChild("nose",
                ModelPartBuilder.create().uv(24, 0)
                        .cuboid(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f, new Dilation(0.0f)),
                ModelTransform.pivot(0.0f, -2.0f, 0.0f));
        head.addChild("hat",
                ModelPartBuilder.create().uv(70, 32)
                        .cuboid(-4.6f, -10.9f, -4.6f, 9.0f, 12.0f, 9.0f, new Dilation(0.0f)),
                ModelTransform.pivot(0.0f, -1.0f, 0.0f));

        ModelPartData body = rootData.addChild("body",
                ModelPartBuilder.create().uv(16, 20)
                        .cuboid(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f, new Dilation(0.0f))
                        .uv(0, 38)
                        .cuboid(-4.0f, 0.0f, -3.0f, 8.0f, 18.0f, 6.0f, new Dilation(0.25f)),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        body.addChild("Cape",
                ModelPartBuilder.create().uv(99, 16)
                        .cuboid(-4.5f, -1.0f, 0.0f, 9.0f, 20.0f, 1.0f, new Dilation(0.0f)),
                ModelTransform.pivot(-0.5f, 2.0f, 3.0f));

        rootData.addChild("right_arm",
                ModelPartBuilder.create().uv(40, 46)
                        .cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new Dilation(0.0f)),
                ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        rootData.addChild("left_arm",
                ModelPartBuilder.create().uv(40, 46).mirrored()
                        .cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new Dilation(0.0f)).mirrored(false),
                ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        rootData.addChild("left_leg",
                ModelPartBuilder.create().uv(0, 22).mirrored()
                        .cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new Dilation(0.0f)).mirrored(false),
                ModelTransform.pivot(2.0f, 12.0f, 0.0f));
        rootData.addChild("right_leg",
                ModelPartBuilder.create().uv(0, 22)
                        .cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new Dilation(0.0f)),
                ModelTransform.pivot(-2.0f, 12.0f, 0.0f));

        return TexturedModelData.of(modelData, 128, 64);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(IceologerEntity entity, float limbAngle, float limbDistance, float animationProgress,
            float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.updateAnimation(entity.idleAnimationState, IceologerAnimations.IDLE, animationProgress, 1.0f);
        this.animateMovement(IceologerAnimations.WALK, limbAngle, limbDistance, 1.0f, 1.0f);
    }
}
