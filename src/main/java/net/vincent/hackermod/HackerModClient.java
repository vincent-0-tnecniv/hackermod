package net.vincent.hackermod;

import net.fabricmc.api.ClientModInitializer;
import net.vincent.hackermod.networking.ModPackets;

public class HackerModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModPackets.registerClientPackets();
    }
}
