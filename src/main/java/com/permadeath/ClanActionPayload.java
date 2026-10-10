package com.permadeath;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public record ClanActionPayload(String action, String value) implements CustomPayload {
 public static final Id<ClanActionPayload> ID = new Id<>(new Identifier(PermadeathMod.MOD_ID,"clan_action"));
 public static final PacketCodec<RegistryByteBuf,ClanActionPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING,ClanActionPayload::action,PacketCodecs.STRING,ClanActionPayload::value,ClanActionPayload::new);
 public Id<? extends CustomPayload> getId(){return ID;}
}
