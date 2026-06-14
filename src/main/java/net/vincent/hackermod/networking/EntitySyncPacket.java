package net.vincent.hackermod.networking;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public record EntitySyncPacket(int entityId, NbtCompound nbt) implements CustomPayload {
    public static final CustomPayload.Id<EntitySyncPacket> ID =
            new CustomPayload.Id<>(Identifier.of(HackerMod.MOD_ID, "entity_sync"));

    public static final PacketCodec<RegistryByteBuf, EntitySyncPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, EntitySyncPacket::entityId,
                    PacketCodecs.NBT_COMPOUND, EntitySyncPacket::nbt,
                    EntitySyncPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}