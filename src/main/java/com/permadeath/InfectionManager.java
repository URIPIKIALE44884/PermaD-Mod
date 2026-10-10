package com.permadeath;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Infeccion zombi:
 * - cuenta los golpes DIRECTOS de zombies al jugador (lo que bloquea el escudo no cuenta);
 * - si pasan N segundos sin recibir otro golpe de zombie, el contador vuelve a 0;
 * - al llegar al limite aplica Zombificacion (sin regeneracion, mata al llegar a 0);
 * - manda al cliente el conteo y el temporizador para mostrarlos en pantalla.
 */
public final class InfectionManager {
    private InfectionManager() {}

    /** jugador -> [golpes acumulados, tick del ultimo golpe] */
    private static final Map<UUID, int[]> HITS = new ConcurrentHashMap<>();
    private static final Map<UUID, int[]> LAST_SENT = new ConcurrentHashMap<>();

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (amount > 0 && entity instanceof ServerPlayerEntity player && source.getAttacker() instanceof MobEntity attacker) {
                onHit(player, attacker, source);
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(InfectionManager::tick);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUuid();
            HITS.remove(id);
            LAST_SENT.remove(id);
        });
    }

    /** Replica la logica del juego: el escudo bloquea si esta levantado y el golpe viene de frente. */
    private static boolean isShieldBlocked(ServerPlayerEntity player, DamageSource source) {
        if (!player.isBlocking() || source.isIn(DamageTypeTags.BYPASSES_SHIELD)) {
            return false;
        }
        Vec3d from = source.getPosition();
        if (from == null) {
            return false;
        }
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d toPlayer = from.relativize(player.getPos()).normalize();
        toPlayer = new Vec3d(toPlayer.x, 0.0, toPlayer.z);
        return toPlayer.dotProduct(look) < 0.0;
    }

    /** Durante una oleada activa se necesitan 25 golpes en vez de los configurados. */
    private static int requiredHits(ServerPlayerEntity player, GlobalSettings settings) {
        return WaveManager.isActive(player.getUuid()) ? WaveManager.INFECTION_HITS_DURING_WAVE : settings.infectionHits;
    }

    private static void onHit(ServerPlayerEntity player, MobEntity attacker, DamageSource source) {
        // solo golpes directos: lo bloqueado con escudo no cuenta
        if (isShieldBlocked(player, source)) {
            return;
        }
        GlobalSettings settings = ModConfig.global();
        if (!settings.infectionEnabled) {
            return;
        }
        MobKind kind = MobKind.of(attacker.getType());
        if (kind == null || kind.family != MobKind.Family.ZOMBIE) {
            return;
        }
        if (player.hasStatusEffect(ModEffects.ZOMBIFICACION)) {
            return;
        }

        int now = player.getServer().getTicks();
        int window = settings.infectionWindowSeconds * 20;
        int[] state = HITS.computeIfAbsent(player.getUuid(), id -> new int[] { 0, now });
        if (state[0] > 0 && now - state[1] > window) {
            state[0] = 0; // pasaron demasiados segundos desde el ultimo golpe
        }
        state[0]++;
        state[1] = now;

        if (state[0] >= requiredHits(player, settings)) {
            state[0] = 0;
            player.addStatusEffect(new StatusEffectInstance(ModEffects.ZOMBIFICACION,
                    settings.infectionDurationSeconds * 20, 0, false, true, true));
            player.sendMessage(Text.translatable("msg.permadeath.infected"), true);
        }
        sync(player, settings);
    }

    private static void tick(MinecraftServer server) {
        GlobalSettings settings = ModConfig.global();
        int now = server.getTicks();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            StatusEffectInstance effect = player.getStatusEffect(ModEffects.ZOMBIFICACION);
            if (effect != null && effect.getDuration() <= 1 && player.isAlive()
                    && !player.isCreative() && !player.isSpectator()) {
                player.kill();
                continue;
            }
            if (now % 20 == 0) {
                int[] state = HITS.get(player.getUuid());
                if (state != null && state[0] > 0 && now - state[1] > settings.infectionWindowSeconds * 20) {
                    state[0] = 0; // 3 minutos (o lo configurado) sin golpes: el contador se reinicia
                }
                sync(player, settings);
            }
        }
    }

    /** Manda el estado al cliente solo cuando cambia. */
    private static void sync(ServerPlayerEntity player, GlobalSettings settings) {
        int[] state = HITS.get(player.getUuid());
        StatusEffectInstance effect = player.getStatusEffect(ModEffects.ZOMBIFICACION);
        int hitCount = state == null ? 0 : state[0];
        int needed = settings.infectionEnabled ? requiredHits(player, settings) : 0;
        int ticksLeft = effect == null ? 0 : effect.getDuration();
        int[] now = new int[] { hitCount, needed, ticksLeft / 20 };

        int[] last = LAST_SENT.get(player.getUuid());
        boolean empty = hitCount == 0 && ticksLeft == 0;
        if (last == null ? empty : java.util.Arrays.equals(last, now)) {
            return;
        }
        LAST_SENT.put(player.getUuid(), now);
        ServerPlayNetworking.send(player, new InfectionPayload(hitCount, needed, ticksLeft));
    }
}
