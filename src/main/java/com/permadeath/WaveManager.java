package com.permadeath;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

/**
 * Oleadas. Cada jugador conectado tiene la suya:
 * - cuenta regresiva de 3 minutos (se muestra en rojo y negrita sobre la barra de inventario);
 * - luego aparecen 10 zombies + 12 esqueletos (+ arañas, creepers y esqueletos Wither si estan activados)
 *   en tandas, entre 10 y 15 bloques del jugador, con la configuracion de spawn de cada mob;
 * - los mobs solo persiguen a ese jugador aunque los ataquen;
 * - ponen andamios para subir y rompen bloques (velocidad de herramienta de diamante);
 * - durante la oleada la Zombificacion necesita 25 golpes.
 */
public final class WaveManager {
    private WaveManager() {}

    public static final String TAG = "pm_wave";
    public static final int INFECTION_HITS_DURING_WAVE = 25;

    /** Explosion de los creepers de la oleada: solo dana a jugadores. */
    public static final ExplosionBehavior PLAYER_ONLY_EXPLOSION = new ExplosionBehavior() {
        @Override
        public boolean shouldDamage(Explosion explosion, Entity entity) {
            return entity instanceof PlayerEntity;
        }
    };

    private static final int COUNTDOWN_TICKS = 3 * 60 * 20;
    private static final int BATCH_SIZE = 3;
    private static final int BATCH_INTERVAL = 60;
    private static final int RADIUS_MIN = 10;
    private static final int RADIUS_MAX = 15;
    private static final int MAX_SCAFFOLDS = 40;

    private static final Random RANDOM = new Random();
    private static final Map<UUID, Wave> WAVES = new ConcurrentHashMap<>();
    private static final Set<UUID> TRACKED = ConcurrentHashMap.newKeySet();

    private static final class MobState {
        BlockPos breakPos;
        int breakProgress;
        int breakNeeded;
        double lastDistance = -1.0;
        int stuckTicks;
        int scaffolds;
    }

    private static final class Wave {
        final UUID playerId;
        int countdown = COUNTDOWN_TICKS;
        boolean active = false;
        boolean finished = false;
        int spawnCooldown = 0;
        final ArrayDeque<MobKind> queue = new ArrayDeque<>();
        final List<UUID> mobs = new ArrayList<>();
        final Map<UUID, MobState> states = new HashMap<>();
        int lastMode = -1;
        int lastSeconds = -1;
        int lastRemaining = -1;

        Wave(UUID playerId) {
            this.playerId = playerId;
        }
    }

    // ------------------------------------------------------------ consultas
    public static boolean isActive(UUID playerId) {
        Wave wave = WAVES.get(playerId);
        return wave != null && wave.active;
    }

    public static boolean isTracked(UUID mobId) {
        return TRACKED.contains(mobId);
    }

    // ------------------------------------------------------------ inicio y cancelacion
    public static int startAll(MinecraftServer server) {
        int count = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (start(player)) {
                count++;
            }
        }
        return count;
    }

    public static boolean start(ServerPlayerEntity player) {
        if (player.isSpectator() || WAVES.containsKey(player.getUuid())) {
            return false;
        }
        GlobalSettings settings = ModConfig.global();
        List<MobKind> list = new ArrayList<>();
        add(list, MobKind.ZOMBIE, 10);
        add(list, MobKind.SKELETON, 12);
        if (settings.waveSpiders) {
            add(list, MobKind.SPIDER, 5);
        }
        if (settings.waveCreepers) {
            add(list, MobKind.CREEPER, 5);
        }
        if (settings.waveWither) {
            add(list, MobKind.WITHER_SKELETON, 2);
        }
        Collections.shuffle(list, RANDOM);

        Wave wave = new Wave(player.getUuid());
        wave.queue.addAll(list);
        WAVES.put(player.getUuid(), wave);
        player.sendMessage(Text.translatable("msg.permadeath.wave_incoming"), false);
        return true;
    }

    private static void add(List<MobKind> list, MobKind kind, int count) {
        for (int i = 0; i < count; i++) {
            list.add(kind);
        }
    }

    public static void cancelAll(MinecraftServer server) {
        for (UUID id : new ArrayList<>(WAVES.keySet())) {
            cancel(id, server);
        }
    }

    /** Termina la oleada de un jugador y elimina a los mobs que queden. */
    public static void cancel(UUID playerId, MinecraftServer server) {
        Wave wave = WAVES.remove(playerId);
        if (wave == null) {
            return;
        }
        for (UUID mobId : wave.mobs) {
            TRACKED.remove(mobId);
            for (ServerWorld world : server.getWorlds()) {
                Entity entity = world.getEntity(mobId);
                if (entity != null) {
                    entity.discard();
                }
            }
        }
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player != null) {
            ServerPlayNetworking.send(player, new WavePayload(0, 0, 0));
        }
    }

    private static void finish(Wave wave, ServerPlayerEntity player) {
        WAVES.remove(wave.playerId);
        for (UUID mobId : wave.mobs) {
            TRACKED.remove(mobId);
        }
        player.sendMessage(Text.translatable("msg.permadeath.wave_end"), false);
        ServerPlayNetworking.send(player, new WavePayload(0, 0, 0));
    }

    // ------------------------------------------------------------ registro de eventos
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(WaveManager::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> cancel(handler.player.getUuid(), server));
        ServerLifecycleEvents.SERVER_STOPPING.register(WaveManager::cancelAll);
    }

    // ------------------------------------------------------------ tick
    private static void tick(MinecraftServer server) {
        if (WAVES.isEmpty()) {
            return;
        }
        for (Wave wave : new ArrayList<>(WAVES.values())) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(wave.playerId);
            if (player == null || !player.isAlive() || player.isSpectator()) {
                cancel(wave.playerId, server);
                continue;
            }
            ServerWorld world = player.getServerWorld();

            if (!wave.active) {
                wave.countdown--;
                if (wave.countdown <= 0) {
                    wave.active = true;
                    player.sendMessage(Text.translatable("msg.permadeath.wave_start"), false);
                }
            } else {
                tickActive(world, player, wave);
            }

            if (wave.finished) {
                finish(wave, player);
            } else {
                sendHud(player, wave);
            }
        }
    }

    private static void tickActive(ServerWorld world, ServerPlayerEntity player, Wave wave) {
        if (wave.spawnCooldown > 0) {
            wave.spawnCooldown--;
        } else if (!wave.queue.isEmpty()) {
            spawnBatch(world, player, wave);
            wave.spawnCooldown = BATCH_INTERVAL;
        }

        Iterator<UUID> iterator = wave.mobs.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            Entity entity = world.getEntity(id);
            if (!(entity instanceof MobEntity mob) || !mob.isAlive()) {
                iterator.remove();
                TRACKED.remove(id);
                wave.states.remove(id);
                continue;
            }
            // solo persiguen al jugador de la oleada, aunque los ataquen otros
            if (mob.getTarget() != player) {
                mob.setTarget(player);
            }
            handleObstacles(world, player, mob, wave.states.computeIfAbsent(id, key -> new MobState()));
        }

        if (wave.queue.isEmpty() && wave.mobs.isEmpty()) {
            wave.finished = true;
        }
    }

    private static void sendHud(ServerPlayerEntity player, Wave wave) {
        int mode = wave.active ? 2 : 1;
        int seconds = wave.active ? 0 : (wave.countdown + 19) / 20;
        int remaining = wave.mobs.size() + wave.queue.size();
        if (mode != wave.lastMode || seconds != wave.lastSeconds || remaining != wave.lastRemaining) {
            wave.lastMode = mode;
            wave.lastSeconds = seconds;
            wave.lastRemaining = remaining;
            ServerPlayNetworking.send(player, new WavePayload(mode, seconds, remaining));
        }
    }

    // ------------------------------------------------------------ aparicion
    private static void spawnBatch(ServerWorld world, ServerPlayerEntity player, Wave wave) {
        for (int i = 0; i < BATCH_SIZE && !wave.queue.isEmpty(); i++) {
            Vec3d position = findSpawnPosition(world, player);
            if (position == null) {
                return;
            }
            MobKind kind = wave.queue.peek();
            if (spawnMob(world, player, wave, kind, position)) {
                wave.queue.poll();
            }
        }
    }

    private static Vec3d findSpawnPosition(ServerWorld world, ServerPlayerEntity player) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0;
            double radius = RADIUS_MIN + RANDOM.nextDouble() * (RADIUS_MAX - RADIUS_MIN);
            int x = MathHelper.floor(player.getX() + Math.cos(angle) * radius);
            int z = MathHelper.floor(player.getZ() + Math.sin(angle) * radius);
            int baseY = player.getBlockY();
            for (int dy = 6; dy >= -8; dy--) {
                BlockPos pos = new BlockPos(x, baseY + dy, z);
                BlockPos below = pos.down();
                if (world.isAir(pos) && world.isAir(pos.up()) && world.getFluidState(pos).isEmpty()
                        && world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP)) {
                    return new Vec3d(x + 0.5, pos.getY(), z + 0.5);
                }
            }
        }
        return null;
    }

    private static boolean spawnMob(ServerWorld world, ServerPlayerEntity player, Wave wave, MobKind kind, Vec3d position) {
        Entity entity = kind.type.create(world);
        if (!(entity instanceof MobEntity mob)) {
            return false;
        }
        mob.refreshPositionAndAngles(position.x, position.y, position.z, RANDOM.nextFloat() * 360.0f, 0.0f);
        // initialize marca al mob como "nuevo": asi recibe la configuracion de spawn del menu
        mob.initialize(world, world.getLocalDifficulty(mob.getBlockPos()), SpawnReason.EVENT, null);
        mob.addCommandTag(TAG);
        mob.setPersistent();

        EntityAttributeInstance follow = mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (follow != null) {
            follow.setBaseValue(64.0);
        }
        if (kind == MobKind.SPIDER) {
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 0, false, false));
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, StatusEffectInstance.INFINITE, 1, false, false));
        }
        mob.setTarget(player);

        wave.mobs.add(mob.getUuid());
        TRACKED.add(mob.getUuid());
        world.spawnEntityAndPassengers(mob);
        return true;
    }

    // ------------------------------------------------------------ andamios y romper bloques
    private static void handleObstacles(ServerWorld world, ServerPlayerEntity player, MobEntity mob, MobState state) {
        if (state.breakPos != null) {
            continueBreaking(world, mob, state);
            return;
        }
        if (world.getTime() % 10 != 0) {
            return;
        }

        double distance = mob.distanceTo(player);
        boolean progressing = state.lastDistance < 0 || distance < state.lastDistance - 0.3;
        state.lastDistance = distance;
        if (distance < 2.5 || (progressing && !mob.getNavigation().isIdle())) {
            state.stuckTicks = 0;
            return;
        }

        state.stuckTicks += 10;
        if (state.stuckTicks < 30) {
            return;
        }

        double dy = player.getY() - mob.getY();
        double horizontal = Math.hypot(player.getX() - mob.getX(), player.getZ() - mob.getZ());
        BlockPos feet = mob.getBlockPos();
        if (dy >= 2.0 && horizontal < 10.0 && state.scaffolds < MAX_SCAFFOLDS
                && world.isAir(feet) && world.isAir(feet.up()) && world.isAir(feet.up(2))) {
            placeScaffold(world, mob, state, feet);
            return;
        }
        startBreaking(world, player, mob, state, dy);
    }

    private static void placeScaffold(ServerWorld world, MobEntity mob, MobState state, BlockPos feet) {
        world.setBlockState(feet, Blocks.SCAFFOLDING.getDefaultState().with(ScaffoldingBlock.DISTANCE, 0));
        mob.requestTeleport(mob.getX(), feet.getY() + 1.0, mob.getZ());
        state.scaffolds++;
        state.stuckTicks = 20;
    }

    private static void startBreaking(ServerWorld world, ServerPlayerEntity player, MobEntity mob, MobState state, double dy) {
        double dx = player.getX() - mob.getX();
        double dz = player.getZ() - mob.getZ();
        int stepX = 0;
        int stepZ = 0;
        if (Math.abs(dx) > Math.abs(dz)) {
            stepX = dx > 0 ? 1 : -1;
        } else {
            stepZ = dz > 0 ? 1 : -1;
        }

        BlockPos feet = mob.getBlockPos();
        List<BlockPos> candidates = new ArrayList<>();
        candidates.add(feet.add(stepX, 0, stepZ));
        candidates.add(feet.add(stepX, 1, stepZ));
        if (dy >= 1.5) {
            candidates.add(feet.up(2));
        }
        if (dy <= -1.5) {
            candidates.add(feet.down());
        }

        for (BlockPos pos : candidates) {
            BlockState block = world.getBlockState(pos);
            if (block.isAir() || !block.getFluidState().isEmpty() || block.isOf(Blocks.SCAFFOLDING)) {
                continue;
            }
            int needed = breakTicks(world, pos, block);
            if (needed < 0) {
                continue;
            }
            state.breakPos = pos;
            state.breakNeeded = needed;
            state.breakProgress = 0;
            return;
        }
    }

    /** Ticks para romper un bloque: herramienta de diamante si corresponde (pico, hacha o pala), mano si no. */
    private static int breakTicks(ServerWorld world, BlockPos pos, BlockState block) {
        float hardness = block.getHardness(world, pos);
        if (hardness < 0) {
            return -1;
        }
        boolean tool = block.isIn(BlockTags.PICKAXE_MINEABLE) || block.isIn(BlockTags.AXE_MINEABLE)
                || block.isIn(BlockTags.SHOVEL_MINEABLE);
        float speed = tool ? 8.0f : 1.0f;
        float factor = tool ? 30.0f : 100.0f;
        return Math.max(1, (int) Math.ceil(hardness * factor / speed));
    }

    private static void continueBreaking(ServerWorld world, MobEntity mob, MobState state) {
        BlockState block = world.getBlockState(state.breakPos);
        if (block.isAir() || mob.squaredDistanceTo(Vec3d.ofCenter(state.breakPos)) > 16.0) {
            world.setBlockBreakingInfo(mob.getId(), state.breakPos, -1);
            state.breakPos = null;
            return;
        }
        state.breakProgress++;
        if (state.breakProgress % 6 == 0) {
            mob.swingHand(Hand.MAIN_HAND);
        }
        int stage = Math.min(9, state.breakProgress * 10 / Math.max(1, state.breakNeeded));
        world.setBlockBreakingInfo(mob.getId(), state.breakPos, stage);
        mob.getNavigation().stop();

        if (state.breakProgress >= state.breakNeeded) {
            world.setBlockBreakingInfo(mob.getId(), state.breakPos, -1);
            world.breakBlock(state.breakPos, false, mob);
            state.breakPos = null;
            state.stuckTicks = 0;
        }
    }
}
