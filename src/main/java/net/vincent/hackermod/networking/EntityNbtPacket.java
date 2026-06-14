package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record EntityNbtPacket(int entityId, String key, String value, byte type) implements CustomPayload {
    public static final Id<EntityNbtPacket> ID =
            new Id<>(Identifier.of(HackerMod.MOD_ID, "entity_nbt"));

    public static final PacketCodec<RegistryByteBuf, EntityNbtPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, EntityNbtPacket::entityId,
                    PacketCodecs.STRING, EntityNbtPacket::key,
                    PacketCodecs.STRING, EntityNbtPacket::value,
                    PacketCodecs.BYTE, EntityNbtPacket::type,
                    EntityNbtPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}