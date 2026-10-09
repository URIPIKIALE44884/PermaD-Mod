package com.permadeath;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Iglu pequeno propio del Iceologer. La probabilidad por chunk nuevo se configura en el menu
 * (pestana Illagers). Dentro aparecen 1 o 2 Iceologers que no se van.
 */
public class IceologerIglooFeature extends Feature<DefaultFeatureConfig> {
    private static final double RADIUS = 3.6;

    public IceologerIglooFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int chance = ModConfig.global().iglooChance;
        Random random = context.getRandom();
        if (chance <= 0 || random.nextInt(100) >= chance) {
            return false;
        }

        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        BlockPos floor = origin.down();
        if (!world.getBlockState(floor).isSolidBlock(world, floor) || !world.getFluidState(origin).isEmpty()) {
            return false;
        }
        // terreno razonablemente plano
        int[][] corners = { { -3, -3 }, { 3, -3 }, { -3, 3 }, { 3, 3 } };
        for (int[] corner : corners) {
            int height = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX() + corner[0], origin.getZ() + corner[1]);
            if (Math.abs(height - origin.getY()) > 3) {
                return false;
            }
        }

        build(world, origin);

        ServerWorld serverWorld = world.toServerWorld();
        int count = 1 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            IceologerEntity iceologer = ModEntities.ICEOLOGER.create(serverWorld);
            if (iceologer == null) {
                continue;
            }
            iceologer.refreshPositionAndAngles(origin.getX() + 0.5 + (i == 0 ? 0.0 : 1.0), origin.getY(),
                    origin.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            iceologer.initialize(world, world.getLocalDifficulty(iceologer.getBlockPos()), SpawnReason.STRUCTURE, null);
            iceologer.setPersistent();
            world.spawnEntityAndPassengers(iceologer);
        }
        return true;
    }

    private static void build(StructureWorldAccess world, BlockPos origin) {
        BlockState snow = Blocks.SNOW_BLOCK.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        int baseY = origin.getY();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double flat = dx * dx + dz * dz;
                // piso (reemplaza el bloque de suelo)
                if (flat <= RADIUS * RADIUS) {
                    boolean center = dx == 0 && dz == 0;
                    set(world, new BlockPos(origin.getX() + dx, baseY - 1, origin.getZ() + dz),
                            center ? Blocks.PACKED_ICE.getDefaultState() : snow);
                }
                // cupula: pared de nieve y hueco interior
                for (int dy = 0; dy <= 4; dy++) {
                    double dist = Math.sqrt(flat + dy * dy);
                    BlockPos pos = new BlockPos(origin.getX() + dx, baseY + dy, origin.getZ() + dz);
                    if (dist <= RADIUS - 1.15) {
                        set(world, pos, air);
                    } else if (dist <= RADIUS) {
                        set(world, pos, snow);
                    }
                }
            }
        }

        // puerta hacia el norte (2 bloques de alto)
        for (int dz = -4; dz <= -2; dz++) {
            for (int dy = 0; dy <= 1; dy++) {
                set(world, new BlockPos(origin.getX(), baseY + dy, origin.getZ() + dz), air);
            }
        }
        // ventanas de hielo
        set(world, new BlockPos(origin.getX() + 3, baseY + 1, origin.getZ()), Blocks.ICE.getDefaultState());
        set(world, new BlockPos(origin.getX() - 3, baseY + 1, origin.getZ()), Blocks.ICE.getDefaultState());
    }

    private static void set(StructureWorldAccess world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
    }
}
