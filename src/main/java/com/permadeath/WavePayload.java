package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WavePayload(int mode, int seconds, int remaining) implements CustomPayload {
    public static final CustomPayload.Id<WavePayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "wave"));

    public static final PacketCodec<RegistryByteBuf, WavePayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, WavePayload::mode, PacketCodecs.INTEGER, WavePayload::seconds, PacketCodecs.INTEGER, WavePayload::remaining, WavePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
