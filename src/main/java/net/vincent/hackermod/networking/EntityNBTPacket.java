package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record EntityNBTPacket(int entityId, double x, double y, double z) implements CustomPayload {
    public static final CustomPayload.Id<EntityNBTPacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "entity_update"));

    public static final PacketCodec<RegistryByteBuf, EntityNBTPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, EntityNBTPacket::entityId,
                    PacketCodecs.DOUBLE, EntityNBTPacket::x,
                    PacketCodecs.DOUBLE, EntityNBTPacket::y,
                    PacketCodecs.DOUBLE, EntityNBTPacket::z,
                    EntityNBTPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}