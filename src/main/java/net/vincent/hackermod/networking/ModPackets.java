package net.vincent.hackermod.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public class ModPackets {
    public static void registerPackets(){

        PayloadTypeRegistry.playC2S().register(BlockStateUpdatePacket.ID, BlockStateUpdatePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BlockStateUpdatePacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                var pos = packet.pos();
                var propertyName = packet.propertyName();
                var propertyValue = packet.propertyValue();
                var state = world.getBlockState(pos);

                boolean success = false;
                for (Property<?> property : state.getProperties()) {
                    if (property.getName().equals(propertyName)) {
                        success = applyPropertyChange(state, property, propertyValue, world, pos);
                        break;
                    }
                }

                if (success) {
                    context.player().sendMessage(Text.literal("§aUpdated " + propertyName + " to " + propertyValue), true);
                } else {
                    context.player().sendMessage(Text.literal("§cFailed to update " + propertyName), true);
                }
            });
        });

        // 2. Block Update Packet (change entire block)
        PayloadTypeRegistry.playC2S().register(BlockUpdatePacket.ID, BlockUpdatePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BlockUpdatePacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                var pos = packet.pos();
                var newBlockId = packet.newBlockId();

                // Convert string to Identifier
                Identifier blockIdentifier = newBlockId.contains(":") ?
                        Identifier.of(newBlockId) :
                        Identifier.of("minecraft", newBlockId);

                Block newBlock = Registries.BLOCK.get(blockIdentifier);

                if (newBlock != null) {
                    BlockState newState = newBlock.getDefaultState();
                    world.setBlockState(pos, newState);
                    context.player().sendMessage(Text.literal("§aBlock changed to " + newBlockId), true);
                } else {
                    context.player().sendMessage(Text.literal("§cInvalid block: " + newBlockId), true);
                }
            });
        });

        // 3. Entity Summon Packet
        PayloadTypeRegistry.playC2S().register(EntitySummonPacket.ID, EntitySummonPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntitySummonPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                var pos = packet.pos();
                var entityId = packet.entityId();

                // Convert string to EntityType
                Identifier entityIdentifier = entityId.contains(":") ?
                        Identifier.of(entityId) :
                        Identifier.of("minecraft", entityId);

                EntityType<?> entityType = Registries.ENTITY_TYPE.get(entityIdentifier);

                if (entityType == null) {
                    context.player().sendMessage(Text.literal("§cInvalid entity: " + entityId), true);
                    return;
                }

                // Create and spawn entity at the block position
                Entity entity = entityType.create(world);
                if (entity != null) {
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 1.0;
                    double z = pos.getZ() + 0.5;

                    entity.setPosition(x, y, z);
                    world.spawnEntity(entity);

                    context.player().sendMessage(
                            Text.literal("§aSummoned " + entityId + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                            true
                    );
                } else {
                    context.player().sendMessage(Text.literal("§cFailed to create entity"), true);
                }
            });
        });

        PayloadTypeRegistry.playC2S().register(CommandPacket.ID, CommandPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CommandPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                String command = packet.command();
                context.player().getServer().getCommandManager().executeWithPrefix(
                        context.player().getCommandSource(), command
                );
            });
        });

        HackerMod.LOGGER.info("Registering Packets for " + HackerMod.MOD_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean applyPropertyChange(BlockState state, Property property, String valueStr,
                                               net.minecraft.world.World world, BlockPos pos) {
        try {
            if (property.getType() == Boolean.class) {
                boolean boolValue = Boolean.parseBoolean(valueStr);
                BlockState newState = state.with((Property<Boolean>) property, boolValue);
                world.setBlockState(pos, newState);
                return true;
            } else if (property.getType() == Integer.class) {
                int intValue = Integer.parseInt(valueStr);
                BlockState newState = state.with((Property<Integer>) property, intValue);
                world.setBlockState(pos, newState);
                return true;
            } else {
                var parsedValue = property.parse(valueStr.toLowerCase());
                if (parsedValue.isPresent()) {
                    BlockState newState = state.with((Property) property, (Comparable) parsedValue.get());
                    world.setBlockState(pos, newState);
                    return true;
                }
            }
        } catch (Exception e) {
            HackerMod.LOGGER.error("Failed to apply property", e);
        }
        return false;
    }
}
