/*
 * Derivado de "Iceologer Mod" (NeoForge 1.21.4) de CoverWeb y MCreator, licencia Microsoft Reciprocal
 * License (Ms-RL). Los creditos pertenecen a Mojang. Este archivo es un port a Fabric 1.20.6 (nombres
 * Yarn) de los datos originales y se mantiene bajo la Ms-RL: ver LICENSE-MS-RL.txt y NOTICE.md.
 */
package com.permadeath.client;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

/** Animaciones originales del Iceologer: caminar (WALK) y reposo (IDLE). */
public final class IceologerAnimations {
    private IceologerAnimations() {}

    public static final Animation WALK = Animation.Builder.create(1.0f).looping()
            .addBoneAnimation("head", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(7.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("head", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -3.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -3.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -2.75f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -3.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("hat", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("hat", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.4f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.3f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("body", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(12.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(10.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(12.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(12.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -3.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -3.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(-15.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(40.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(-32.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(-15.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, -1.0f, -2.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createTranslationalVector(0.0f, -1.0f, -2.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(30.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(-25.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(42.1459f, -1.6834f, 1.8819f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(30.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("right_arm", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, -1.0f, -2.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createTranslationalVector(0.0f, -1.0f, -2.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("left_leg", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(10.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.2917f, AnimationHelper.createRotationalVector(-11.36f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(-20.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(17.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(10.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("right_leg", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(-17.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(10.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(-20.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(-17.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("Cape", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(18.1091f, 2.0059f, -2.3043f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createRotationalVector(8.1091f, 2.0059f, -2.3043f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9167f, AnimationHelper.createRotationalVector(28.1091f, 2.0059f, -2.3043f), Transformation.Interpolations.LINEAR),
                        new Keyframe(1.0f, AnimationHelper.createRotationalVector(18.1091f, 2.0059f, -2.3043f), Transformation.Interpolations.LINEAR)
                    }))
            .build();

    public static final Animation IDLE = Animation.Builder.create(0.9583f).looping()
            .addBoneAnimation("head", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -2.2f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 1.4f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -2.2f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("hat", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.375f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.17f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("body", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(10.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createRotationalVector(-7.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createRotationalVector(10.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -2.2f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 1.8f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, -2.2f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(0.0f, 0.0f, -17.5f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createRotationalVector(0.0f, 0.0f, -40.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createRotationalVector(0.0f, 0.0f, -17.5f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 2.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 17.5f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createRotationalVector(20.1739f, -10.1778f, 43.1848f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createRotationalVector(0.0f, 0.0f, 17.5f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("right_arm", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 2.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .addBoneAnimation("Cape", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe[] {
                        new Keyframe(0.0f, AnimationHelper.createRotationalVector(22.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.4167f, AnimationHelper.createRotationalVector(55.0f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR),
                        new Keyframe(0.9583f, AnimationHelper.createRotationalVector(22.5f, 0.0f, 0.0f), Transformation.Interpolations.LINEAR)
                    }))
            .build();
}
