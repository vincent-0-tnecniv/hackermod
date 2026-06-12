package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record CommandPacket(String command) implements CustomPayload {
    public static final CustomPayload.Id<CommandPacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "command"));

    public static final PacketCodec<RegistryByteBuf, CommandPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, CommandPacket::command,
                    CommandPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}