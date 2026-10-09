package com.permadeath;

import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * - Un arco con Multishot dispara 3 a 5 flechas en abanico (la central es la normal y las demas
 *   no se pueden recoger, como en la ballesta). Gasta una sola flecha.
 * - El Ilusioner suelta un arco con Multishot al morir.
 */
public final class BowMultishot {
    private BowMultishot() {}

    private static final String TAG = "pm_multishot";

    public static ItemStack createBow() {
        ItemStack bow = new ItemStack(Items.BOW);
        bow.addEnchantment(ModEnchantments.BOW_MULTISHOT, 1);
        return bow;
    }

    public static void init() {
        // flechas nuevas disparadas por un jugador que sostiene un arco con Multishot
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PersistentProjectileEntity arrow) || arrow.getCommandTags().contains(TAG)) {
                return;
            }
            if (!(arrow.getOwner() instanceof ServerPlayerEntity player) || !player.isUsingItem()) {
                return;
            }
            ItemStack bow = player.getActiveItem();
            if (!bow.isOf(Items.BOW) || EnchantmentHelper.getLevel(ModEnchantments.BOW_MULTISHOT, bow) <= 0) {
                return;
            }
            arrow.addCommandTag(TAG);

            int total = 3 + world.getRandom().nextInt(3); // 3 a 5 flechas
            for (int i = 1; i < total; i++) {
                double degrees = Math.ceil(i / 2.0) * 10.0 * (i % 2 == 0 ? -1.0 : 1.0);
                Entity created = arrow.getType().create(world);
                if (!(created instanceof PersistentProjectileEntity copy)) {
                    continue;
                }
                NbtCompound nbt = new NbtCompound();
                arrow.writeNbt(nbt);
                nbt.remove("UUID");
                copy.readNbt(nbt);
                copy.setUuid(UUID.randomUUID());

                Vec3d velocity = arrow.getVelocity().rotateY((float) Math.toRadians(degrees));
                copy.setVelocity(velocity);
                copy.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                copy.addCommandTag(TAG);
                world.spawnEntity(copy);
            }
        });

        // el Ilusioner suelta un arco con Multishot
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity.getType() == EntityType.ILLUSIONER && !entity.getWorld().isClient) {
                entity.dropStack(createBow());
            }
        });
    }
}
