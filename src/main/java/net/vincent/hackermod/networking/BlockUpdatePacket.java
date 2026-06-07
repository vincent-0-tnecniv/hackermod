package net.vincent.hackermod.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public record BlockUpdatePacket(BlockPos pos, String propertyName, String propertyValue) implements CustomPayload {
    public static final CustomPayload.Id<BlockUpdatePacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "block_update"));

    public static final PacketCodec<PacketByteBuf, BlockUpdatePacket> CODEC =
            PacketCodec.of((value, buf) -> {
                buf.writeBlockPos(value.pos);
                buf.writeString(value.propertyName);
                buf.writeString(value.propertyValue);
            }, buf -> new BlockUpdatePacket(buf.readBlockPos(), buf.readString(), buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}