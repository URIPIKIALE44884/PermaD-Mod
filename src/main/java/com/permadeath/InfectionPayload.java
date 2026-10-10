package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record InfectionPayload(int hits, int needed, int ticksLeft) implements CustomPayload {
    public static final CustomPayload.Id<InfectionPayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "infection"));

    public static final PacketCodec<RegistryByteBuf, InfectionPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, InfectionPayload::hits, PacketCodecs.INTEGER, InfectionPayload::needed, PacketCodecs.INTEGER, InfectionPayload::ticksLeft, InfectionPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
