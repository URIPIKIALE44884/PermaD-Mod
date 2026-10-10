package com.permadeath;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public record ClanOpenPayload(String summary) implements CustomPayload {
 public static final Id<ClanOpenPayload> ID=new Id<>(new Identifier(PermadeathMod.MOD_ID,"clan_open"));
 public static final PacketCodec<RegistryByteBuf,ClanOpenPayload> CODEC=PacketCodec.tuple(PacketCodecs.STRING,ClanOpenPayload::summary,ClanOpenPayload::new);
 public Id<? extends CustomPayload> getId(){return ID;}
}
