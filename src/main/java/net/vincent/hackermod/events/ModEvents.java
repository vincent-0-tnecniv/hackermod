package net.vincent.hackermod.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vincent.hackermod.HackerMod;

public class ModEvents {
    public static void registerEvents() {
        flyerCannotTakeFallDamage();
        HackerMod.LOGGER.info("Registering Mod Events for "+ HackerMod.MOD_ID);
    }

    private static void flyerCannotTakeFallDamage() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getAbilities().allowFlying) {
                    player.fallDistance = 0;
                }
            }
        });
    }
}
