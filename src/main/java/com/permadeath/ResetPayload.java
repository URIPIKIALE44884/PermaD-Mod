package com.permadeath;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ResetPayload(String kind) implements CustomPayload {
    public static final CustomPayload.Id<ResetPayload> ID =
            new CustomPayload.Id<>(new Identifier(PermadeathMod.MOD_ID, "reset"));

    public static final PacketCodec<RegistryByteBuf, ResetPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ResetPayload::kind, ResetPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
