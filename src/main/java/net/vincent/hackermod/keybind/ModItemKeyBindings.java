package net.vincent.hackermod.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.item.HackerHandItem;
import net.vincent.hackermod.networking.FlightUpdatePacket;
import net.vincent.hackermod.screen.HackerHandBlockScreen;
import net.vincent.hackermod.screen.HackerHandCommandScreen;
import net.vincent.hackermod.screen.HackerHandSummonScreen;

public class ModItemKeyBindings {

    private static boolean clientFlightState = false;

    public static void registerItemKeyBindings() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // When V is pressed while holding the H4CKER's hand
            if (ModKeybindings.commandMenuKey.wasPressed()) {
                // Check if player is holding the hacker hand
                if (client.player != null &&
                        client.player.getMainHandStack().getItem() instanceof HackerHandItem) {
                    client.setScreen(new HackerHandCommandScreen());
                }
            }
            // When B is pressed while holding the H4CK3R's hand
            if (ModKeybindings.summonMenuKey.wasPressed() && client.player != null) {
                // If no entity or sneaking, check for block
                HitResult hit = client.player.raycast(500.0, 1.0F, false);
                if (hit.getType() == HitResult.Type.BLOCK && client.player.getMainHandStack().getItem() instanceof HackerHandItem) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    client.setScreen(new HackerHandSummonScreen(blockHit.getBlockPos()));
                }
            }
            if (ModKeybindings.flyToggleKey.wasPressed() && client.player != null && client.player.getMainHandStack().getItem() instanceof HackerHandItem) {
                clientFlightState = !clientFlightState;
                if(clientFlightState) {
                    client.player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                    client.player.sendMessage(Text.literal("Flight Enabled!"));
                } else{
                    client.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
                    client.player.sendMessage(Text.literal("Flight Disabled!"));
                }
                ClientPlayNetworking.send(new FlightUpdatePacket(clientFlightState));
            }
        });

        HackerMod.LOGGER.info("Registering Items Keybinds for " + HackerMod.MOD_ID);
    }
}
