package net.vincent.hackermod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.vincent.hackermod.networking.EntitySyncPacket;
import net.vincent.hackermod.screen.HackerHandEntityNBTScreen;

public class HackerModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(EntitySyncPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                HackerMod.LOGGER.info("CLIENT: Received sync for entity {}", packet.entityId());

                Entity entity = context.client().world.getEntityById(packet.entityId());
                if (entity != null) {
                    // Update the entity with synced NBT
                    entity.readNbt(packet.nbt());
                    HackerMod.LOGGER.info("CLIENT: Synced entity {} NBT", packet.entityId());

                    // Refresh the screen if it's open
                    if (context.client().currentScreen instanceof HackerHandEntityNBTScreen screen) {
                        screen.refreshEntityData();
                    }
                }
            });
        });
    }
}
