package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaveActionPayload(String action) implements CustomPayload {
    public static final CustomPayload.Id<WaveActionPayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "wave_action"));

    public static final PacketCodec<RegistryByteBuf, WaveActionPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, WaveActionPayload::action, WaveActionPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
