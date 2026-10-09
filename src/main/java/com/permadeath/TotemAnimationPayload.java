package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** El servidor avisa al cliente que muestre la animacion del Totem de Memoria al reaparecer. */
public record TotemAnimationPayload() implements CustomPayload {
    public static final CustomPayload.Id<TotemAnimationPayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "totem_animation"));

    public static final PacketCodec<RegistryByteBuf, TotemAnimationPayload> CODEC =
            PacketCodec.unit(new TotemAnimationPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
