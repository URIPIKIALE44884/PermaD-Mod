package com.permadeath;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

/**
 * Fortaleza del Wildfire: plataforma de 35x35, base de obsidiana, piso de tablones carmesies,
 * paredes de ladrillo del Nether, 4 torres de obsidiana llorosa (3x3x5) cada una con un generador
 * de Blaze, y una sala central CERRADA donde aparece el Wildfire. Se conecta con un puente corto
 * a una fortaleza del Nether vanilla ya generada cerca (no genera nunca sola).
 *
 * NOTA TECNICA: las fortalezas del Nether vanilla no usan jigsaw (a diferencia de bastiones o
 * aldeas), asi que no se puede insertar esta sala como una pieza mas de la estructura original.
 * En cambio, se la construye pegada a uno de los lados de la caja (bounding box) de la fortaleza
 * ya generada, con un puente de ladrillo del Nether que las une.
 */
public class WildfireFortressFeature extends Feature<DefaultFeatureConfig> {
    private static final int SIZE = 35;
    private static final int HALF = SIZE / 2;
    private static final int WALL_HEIGHT = 10;

    public WildfireFortressFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int chance = ModConfig.global().wildfireFortressChance;
        Random random = context.getRandom();
        if (chance <= 0 || random.nextInt(100) >= chance) {
            return false;
        }

        StructureWorldAccess world = context.getWorld();
        if (!(world.toServerWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }
        BlockPos origin = context.getOrigin();

        Structure fortress = serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE).get(StructureKeys.FORTRESS);
        if (fortress == null) {
            return false;
        }
        StructureStart start = serverWorld.getStructureAccessor().getStructureAt(origin, fortress);
        if (start == null || !start.hasChildren()) {
            return false;
        }
        BlockBox box = start.getBoundingBox();

        // punto de union: justo al este de la caja de la fortaleza, a la altura de su centro
        int attachX = box.getMaxX() + 3;
        int attachY = (box.getMinY() + box.getMaxY()) / 2;
        int attachZ = (box.getMinZ() + box.getMaxZ()) / 2;

        int centerX = attachX + 8 + HALF;
        BlockPos center = new BlockPos(centerX, attachY, attachZ);

        buildBridge(world, new BlockPos(attachX, attachY, attachZ), new BlockPos(centerX - HALF, attachY, attachZ));
        List<BlockPos> spawners = buildFortress(world, center);

        WildfireEntity wildfire = ModEntities.WILDFIRE.create(serverWorld);
        if (wildfire != null) {
            wildfire.refreshPositionAndAngles(center.getX() + 0.5, attachY + 4, center.getZ() + 0.5, 0.0f, 0.0f);
            wildfire.initialize(world, world.getLocalDifficulty(wildfire.getBlockPos()), SpawnReason.STRUCTURE, null);
            wildfire.setPersistent();
            wildfire.setSpawnerPositions(spawners);
            world.spawnEntityAndPassengers(wildfire);
        }
        return true;
    }

    private static void buildBridge(StructureWorldAccess world, BlockPos from, BlockPos to) {
        int length = to.getX() - from.getX();
        BlockState bricks = Blocks.NETHER_BRICKS.getDefaultState();
        for (int i = 0; i <= Math.max(1, length); i++) {
            BlockPos base = from.add(i, 0, 0);
            for (int dz = -1; dz <= 1; dz++) {
                set(world, base.add(0, -1, dz), bricks);
                set(world, base.add(0, 0, dz), Blocks.AIR.getDefaultState());
                set(world, base.add(0, 1, dz), Blocks.AIR.getDefaultState());
            }
        }
    }

    /** Devuelve las posiciones de los 4 generadores de Blaze, para que el Wildfire los pueda transformar despues. */
    private static List<BlockPos> buildFortress(StructureWorldAccess world, BlockPos center) {
        BlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
        BlockState floor = Blocks.CRIMSON_PLANKS.getDefaultState();
        BlockState wall = Blocks.NETHER_BRICKS.getDefaultState();
        BlockState tower = Blocks.CRYING_OBSIDIAN.getDefaultState();
        int baseY = center.getY();

        // base: obsidiana por fuera, tablones carmesies en la zona caminable
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                boolean edge = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                set(world, center.add(dx, -1, dz), obsidian);
                set(world, center.add(dx, 0, dz), edge ? obsidian : floor);
            }
        }

        // paredes perimetrales con almenas simples
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                if (Math.abs(dx) != HALF && Math.abs(dz) != HALF) {
                    continue;
                }
                for (int dy = 1; dy <= WALL_HEIGHT; dy++) {
                    set(world, center.add(dx, dy, dz), wall);
                }
                if ((dx + dz) % 3 == 0) {
                    set(world, center.add(dx, WALL_HEIGHT + 1, dz), wall);
                }
            }
        }

        // 4 torres de obsidiana llorosa (3x3x5) cerca de las esquinas, cada una con su generador
        List<BlockPos> spawners = new ArrayList<>();
        int inset = HALF - 2;
        int[][] corners = { { -inset, -inset }, { inset, -inset }, { -inset, inset }, { inset, inset } };
        for (int[] corner : corners) {
            BlockPos towerBase = center.add(corner[0] - 1, 0, corner[1] - 1);
            for (int dx = 0; dx < 3; dx++) {
                for (int dz = 0; dz < 3; dz++) {
                    for (int dy = 0; dy < 5; dy++) {
                        set(world, towerBase.add(dx, dy, dz), tower);
                    }
                }
            }
            BlockPos spawnerPos = towerBase.add(1, 5, 1);
            set(world, spawnerPos, Blocks.SPAWNER.getDefaultState());
            BlockEntity entity = world.getBlockEntity(spawnerPos);
            if (entity instanceof MobSpawnerBlockEntity spawnerEntity) {
                spawnerEntity.getLogic().setEntityId(net.minecraft.entity.EntityType.BLAZE, world.toServerWorld(), world.getRandom(), spawnerPos);
            }
            spawners.add(spawnerPos);
        }

        // sala central cerrada (ladrillo del Nether, sin puerta: hay que picar para entrar)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    boolean edgeRoom = Math.abs(dx) == 3 || Math.abs(dz) == 3 || dy == 5;
                    set(world, center.add(dx, dy, dz), edgeRoom ? wall : Blocks.AIR.getDefaultState());
                }
            }
        }

        return spawners;
    }

    private static void set(StructureWorldAccess world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, net.minecraft.block.Block.NOTIFY_LISTENERS);
    }
}
