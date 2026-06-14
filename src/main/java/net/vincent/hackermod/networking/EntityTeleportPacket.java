package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record EntityTeleportPacket(int entityId, double x, double y, double z) implements CustomPayload {
    public static final CustomPayload.Id<EntityTeleportPacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "entity_teleport"));

    public static final PacketCodec<RegistryByteBuf, EntityTeleportPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, EntityTeleportPacket::entityId,
                    PacketCodecs.DOUBLE, EntityTeleportPacket::x,
                    PacketCodecs.DOUBLE, EntityTeleportPacket::y,
                    PacketCodecs.DOUBLE, EntityTeleportPacket::z,
                    EntityTeleportPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}