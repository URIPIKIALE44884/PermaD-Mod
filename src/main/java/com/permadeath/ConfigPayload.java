package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigPayload(String json, boolean open) implements CustomPayload {
    public static final CustomPayload.Id<ConfigPayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "config"));

    public static final PacketCodec<RegistryByteBuf, ConfigPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ConfigPayload::json, PacketCodecs.BOOL, ConfigPayload::open, ConfigPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
