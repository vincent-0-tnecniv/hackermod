package net.vincent.hackermod;

import net.fabricmc.api.ModInitializer;

import net.vincent.hackermod.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HackerMod implements ModInitializer {
	public static final String MOD_ID = "hackermod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
	}
}