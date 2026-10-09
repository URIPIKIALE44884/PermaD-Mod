package com.permadeath;

import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/** Arco Multishot funcional y botin probabilistico del Ilusioner. */
public final class BowMultishot {
    private BowMultishot() {}

    private static final String TAG = "pm_multishot";
    private static final float ILLUSIONER_BOW_DROP_CHANCE = 0.25f;
    private static final double ILLUSIONER_MAX_HEALTH = 60.0;

    public static ItemStack createBow() {
        ItemStack bow = new ItemStack(Items.BOW);
        bow.addEnchantment(ModEnchantments.BOW_MULTISHOT, 1);
        return bow;
    }

    public static void init() {
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

            int total = 3 + world.getRandom().nextInt(3);
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

        // El arco especial cae en un 25% de las muertes del Ilusioner.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof IllusionerEntity && !entity.getWorld().isClient
                    && entity.getRandom().nextFloat() < ILLUSIONER_BOW_DROP_CHANCE) {
                entity.dropStack(createBow());
            }
        });
    }
}
