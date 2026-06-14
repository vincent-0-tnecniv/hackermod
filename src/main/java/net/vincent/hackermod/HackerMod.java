package net.vincent.hackermod;

import net.fabricmc.api.ModInitializer;
import net.vincent.hackermod.events.ModEvents;
import net.vincent.hackermod.item.ModItemGroups;
import net.vincent.hackermod.item.ModItems;
import net.vincent.hackermod.keybind.ModItemKeyBindings;
import net.vincent.hackermod.keybind.ModKeybindings;
import net.vincent.hackermod.networking.ModPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HackerMod implements ModInitializer {
	public static final String MOD_ID = "hackermod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModPackets.registerServerPackets();
		ModItems.registerModItems();
		ModItemGroups.registerModItemGroups();
		ModKeybindings.registerKeys();
		ModItemKeyBindings.registerItemKeyBindings();
		ModEvents.registerEvents();
	}
}