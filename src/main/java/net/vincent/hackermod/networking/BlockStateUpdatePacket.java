package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public record BlockStateUpdatePacket(BlockPos pos, String propertyName, String propertyValue) implements CustomPayload {
    public static final CustomPayload.Id<BlockStateUpdatePacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "blockstate_update"));

    public static final PacketCodec<RegistryByteBuf, BlockStateUpdatePacket> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, BlockStateUpdatePacket::pos,
                    PacketCodecs.STRING, BlockStateUpdatePacket::propertyName,
                    PacketCodecs.STRING, BlockStateUpdatePacket::propertyValue,
                    BlockStateUpdatePacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}