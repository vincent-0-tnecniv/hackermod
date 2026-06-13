package net.vincent.hackermod.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record EntityTransformPacket(int oldEntityId, String newEntityType, double x, double y, double z, float yaw, float pitch) implements CustomPayload {
    public static final CustomPayload.Id<EntityTransformPacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "entity_transform"));

    public static final PacketCodec<PacketByteBuf, EntityTransformPacket> CODEC =
            PacketCodec.of(EntityTransformPacket::write, EntityTransformPacket::new);

    // Constructor for reading from buffer
    public EntityTransformPacket(PacketByteBuf buf) {
        this(buf.readInt(), buf.readString(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat());
    }

    // Method for writing to buffer
    public void write(PacketByteBuf buf) {
        buf.writeInt(oldEntityId);
        buf.writeString(newEntityType);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}