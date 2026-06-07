package net.vincent.hackermod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.item.ModItemGroups;
import net.vincent.hackermod.item.ModItems;
import net.vincent.hackermod.networking.BlockUpdatePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HackerMod implements ModInitializer {
	public static final String MOD_ID = "hackermod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		// Register packet type (C2S = Client to Server)
		PayloadTypeRegistry.playC2S().register(BlockUpdatePacket.ID, BlockUpdatePacket.CODEC);

		// Register the handler for when packet is received
		ServerPlayNetworking.registerGlobalReceiver(BlockUpdatePacket.ID, (packet, context) -> {
			context.server().execute(() -> {
				// Get the world and block state
				var world = context.player().getWorld();
				var pos = packet.pos();
				var state = world.getBlockState(pos);

				// Find and apply the property change
				boolean success = false;
				for (Property<?> property : state.getProperties()) {
					if (property.getName().equals(packet.propertyName())) {
						success = applyPropertyChange(state, property, packet.propertyValue(), world, pos);
						break;
					}
				}

				// Send feedback to player
				if (success) {
					context.player().sendMessage(Text.literal("§aBlock updated!"), true);
				} else {
					context.player().sendMessage(Text.literal("§cFailed to update block! Invalid value?"), true);
				}
			});
		});

		ModItems.registerModItems();
		ModItemGroups.registerModItemGroups();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean applyPropertyChange(BlockState state, Property property, String valueStr,
	                                           net.minecraft.world.World world, BlockPos pos) {
		try {
			// Handle boolean properties (like snowy)
			if (property.getType() == Boolean.class) {
				boolean boolValue = Boolean.parseBoolean(valueStr);
				BlockState newState = state.with((Property<Boolean>) property, boolValue);
				world.setBlockState(pos, newState);
				LOGGER.info("Updated boolean property {} to {}", property.getName(), boolValue);
				return true;
			}

			// Handle integer properties
			if (property.getType() == Integer.class) {
				int intValue = Integer.parseInt(valueStr);
				BlockState newState = state.with((Property<Integer>) property, intValue);
				world.setBlockState(pos, newState);
				LOGGER.info("Updated integer property {} to {}", property.getName(), intValue);
				return true;
			}

			// Handle enum properties (facing, axis, etc.)
			var parsedValue = property.parse(valueStr.toLowerCase());
			if (parsedValue.isPresent()) {
				BlockState newState = state.with((Property) property, (Comparable) parsedValue.get());
				world.setBlockState(pos, newState);
				LOGGER.info("Updated enum property {} to {}", property.getName(), valueStr);
				return true;
			}

		} catch (Exception e) {
			LOGGER.error("Failed to apply property {} = {}", property.getName(), valueStr, e);
		}
		return false;
	}

}