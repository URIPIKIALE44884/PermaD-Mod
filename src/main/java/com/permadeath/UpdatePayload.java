package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdatePayload(String kind, String json) implements CustomPayload {
    public static final CustomPayload.Id<UpdatePayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "update"));

    public static final PacketCodec<RegistryByteBuf, UpdatePayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdatePayload::kind, PacketCodecs.STRING, UpdatePayload::json, UpdatePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
