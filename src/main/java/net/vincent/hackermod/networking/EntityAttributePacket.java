package net.vincent.hackermod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record EntityAttributePacket(double x, double y, double z) implements CustomPayload {
    public static final Id<EntityAttributePacket> ID =
            new Id<>(Identifier.of(HackerMod.MOD_ID, "entity_update"));

    public static final PacketCodec<RegistryByteBuf, EntityAttributePacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE, EntityAttributePacket::x,
                    PacketCodecs.DOUBLE, EntityAttributePacket::y,
                    PacketCodecs.DOUBLE, EntityAttributePacket::z,
                    EntityAttributePacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}