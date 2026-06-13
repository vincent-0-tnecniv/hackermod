package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public record EntityUpdatePacket(int entityId, String nbtPath, String nbtValue) implements CustomPayload {
    public static final CustomPayload.Id<EntityUpdatePacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "entity_update"));

    public static final PacketCodec<RegistryByteBuf, EntityUpdatePacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, EntityUpdatePacket::entityId,
                    PacketCodecs.STRING, EntityUpdatePacket::nbtPath,
                    PacketCodecs.STRING, EntityUpdatePacket::nbtValue,
                    EntityUpdatePacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}