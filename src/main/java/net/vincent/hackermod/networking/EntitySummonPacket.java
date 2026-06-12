package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public record EntitySummonPacket(BlockPos pos, String entityId) implements CustomPayload {
    public static final CustomPayload.Id<EntitySummonPacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "entity_summon"));

    public static final PacketCodec<RegistryByteBuf, EntitySummonPacket> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, EntitySummonPacket::pos,
                    PacketCodecs.STRING, EntitySummonPacket::entityId,
                    EntitySummonPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}