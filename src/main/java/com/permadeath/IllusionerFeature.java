package com.permadeath;

import com.mojang.serialization.Codec;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Un Ilusioner solitario en el bosque oscuro. Probabilidad por chunk nuevo configurable en el menu. */
public class IllusionerFeature extends Feature<DefaultFeatureConfig> {
    public IllusionerFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int chance = ModConfig.global().illusionerNaturalChance;
        Random random = context.getRandom();
        if (chance <= 0 || random.nextInt(100) >= chance) {
            return false;
        }
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        BlockPos floor = origin.down();
        if (!world.getBlockState(floor).isSolidBlock(world, floor) || !world.getFluidState(origin).isEmpty()
                || !world.isAir(origin) || !world.isAir(origin.up())) {
            return false;
        }
        ServerWorld serverWorld = world.toServerWorld();
        IllusionerEntity illusioner = EntityType.ILLUSIONER.create(serverWorld);
        if (illusioner == null) {
            return false;
        }
        illusioner.refreshPositionAndAngles(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5,
                random.nextFloat() * 360.0f, 0.0f);
        illusioner.initialize(world, world.getLocalDifficulty(illusioner.getBlockPos()), SpawnReason.STRUCTURE, null);
        illusioner.setPersistent();
        world.spawnEntityAndPassengers(illusioner);
        return true;
    }
}
