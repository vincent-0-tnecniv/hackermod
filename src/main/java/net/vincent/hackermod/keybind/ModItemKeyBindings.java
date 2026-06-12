package net.vincent.hackermod.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.item.HackerHandItem;
import net.vincent.hackermod.screen.HackerHandCommandScreen;

public class ModItemKeyBindings {
    public static void registerItemKeyBindings() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // When V is pressed while holding the H4CKER's hand
            if (ModKeybindings.hackerMenuKey.wasPressed()) {
                // Check if player is holding the hacker hand
                if (client.player != null &&
                        client.player.getMainHandStack().getItem() instanceof HackerHandItem) {
                    client.setScreen(new HackerHandCommandScreen());
                }
            }
        });

        HackerMod.LOGGER.info("Registering Items Keybinds for " + HackerMod.MOD_ID);
    }
}
