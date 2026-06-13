package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record FlightUpdatePacket(boolean enableFlight) implements CustomPayload {
    public static final CustomPayload.Id<FlightUpdatePacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "flight_update"));

    public static final PacketCodec<RegistryByteBuf, FlightUpdatePacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL, FlightUpdatePacket::enableFlight,
                    FlightUpdatePacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}