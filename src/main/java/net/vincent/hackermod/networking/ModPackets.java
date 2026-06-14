package net.vincent.hackermod.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        HackerMod.LOGGER.info("Registered Block Property Packets!");

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
        HackerMod.LOGGER.info("Registered General Block Packets!");

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
        HackerMod.LOGGER.info("Registered Entity Summon Packets!");

        PayloadTypeRegistry.playC2S().register(CommandPacket.ID, CommandPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CommandPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                String command = packet.command();
                context.player().getServer().getCommandManager().executeWithPrefix(
                        context.player().getCommandSource(), command
                );
            });
        });
        HackerMod.LOGGER.info("Registered Command Packets!");

        PayloadTypeRegistry.playC2S().register(EntityTeleportPacket.ID, EntityTeleportPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntityTeleportPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                var entity = world.getEntityById(packet.entityId());
                if (entity != null) {
                    entity.setPos(packet.x(), packet.y(), packet.z());
                }
            });
        });
        HackerMod.LOGGER.info("Registered Entity Teleport Packets!");

        // At the start of your EntityNbtPacket server handler

        PayloadTypeRegistry.playC2S().register(EntityNbtPacket.ID, EntityNbtPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EntitySyncPacket.ID, EntitySyncPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntityNbtPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerWorld world = context.player().getServerWorld();
                Entity entity = world.getEntityById(packet.entityId());

                if (entity != null) {
                    HackerMod.LOGGER.info("SERVER: Updating entity {} - {} = {}",
                            packet.entityId(), packet.key(), packet.value());

                    // Apply the change
                    NbtCompound nbt = new NbtCompound();
                    entity.writeNbt(nbt);
                    setNbtValue(nbt, packet.key(), packet.value(), packet.type());
                    entity.readNbt(nbt);

                    // Create sync packet with updated NBT
                    NbtCompound syncNbt = new NbtCompound();
                    entity.writeNbt(syncNbt);

                    HackerMod.LOGGER.info("SERVER: Sending sync packet to client - Invulnerable: {}",
                            syncNbt.getBoolean("Invulnerable"));

                    // Send back to the client
                    ServerPlayNetworking.send(context.player(), new EntitySyncPacket(entity.getId(), syncNbt));
                }
            });
        });
        HackerMod.LOGGER.info("Registered Entity NBT Packets!");

        PayloadTypeRegistry.playC2S().register(FlightUpdatePacket.ID, FlightUpdatePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FlightUpdatePacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();

                player.getAbilities().allowFlying = packet.enableFlight();
                if (packet.enableFlight()) {
                    player.getAbilities().flying = true;
                    player.fallDistance = 0;
                } else {
                    player.getAbilities().flying = false;
                }
                player.sendAbilitiesUpdate();
            });
        });
        HackerMod.LOGGER.info("Registered Flight Toggle Packets!");

        // Entity Transform Packet (server-side spawning)
        PayloadTypeRegistry.playC2S().register(EntityTransformPacket.ID, EntityTransformPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntityTransformPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                var oldEntity = world.getEntityById(packet.oldEntityId());

                // Remove old entity
                if (oldEntity != null) {
                    oldEntity.discard();
                }

                // Get new entity type
                Identifier entityId = packet.newEntityType().contains(":") ?
                        Identifier.of(packet.newEntityType()) :
                        Identifier.of("minecraft", packet.newEntityType());

                EntityType<?> entityType = Registries.ENTITY_TYPE.get(entityId);

                if (entityType == null) {
                    context.player().sendMessage(Text.literal("§cInvalid entity type: " + packet.newEntityType()), true);
                    return;
                }

                // Create new entity
                Entity newEntity = entityType.create(world);
                if (newEntity == null) {
                    context.player().sendMessage(Text.literal("§cFailed to create entity"), true);
                    return;
                }

                // Set position and rotation
                newEntity.setPosition(packet.x(), packet.y(), packet.z());
                newEntity.setYaw(packet.yaw());
                newEntity.setPitch(packet.pitch());

                // Copy custom name from old entity if it existed
                if (oldEntity != null && oldEntity.hasCustomName()) {
                    newEntity.setCustomName(oldEntity.getCustomName());
                    newEntity.setCustomNameVisible(oldEntity.isCustomNameVisible());
                }

                // Spawn the entity
                boolean spawned = world.spawnEntity(newEntity);

                if (spawned) {
                    context.player().sendMessage(
                            Text.literal("§aTransformed entity to " + packet.newEntityType()),
                            true
                    );
                    HackerMod.LOGGER.info("Successfully spawned: {}", packet.newEntityType());
                } else {
                    context.player().sendMessage(Text.literal("§cFailed to spawn entity (block collision?)"), true);
                    HackerMod.LOGGER.error("Failed to spawn entity: {}", packet.newEntityType());
                }
            });
        });
        HackerMod.LOGGER.info("Registered Entity Transformation Packets!");

        HackerMod.LOGGER.info("Registered Packets for " + HackerMod.MOD_ID);
    }

    private static void setNbtValue(NbtCompound nbt, String key, String valueStr, byte type) {
        try {
            switch (type) {
                case 1: // Boolean
                    nbt.putBoolean(key, Boolean.parseBoolean(valueStr));
                    break;
                case 2: // Byte
                    nbt.putByte(key, Byte.parseByte(valueStr));
                    break;
                case 3: // Short
                    nbt.putShort(key, Short.parseShort(valueStr));
                    break;
                case 4: // Int
                    nbt.putInt(key, Integer.parseInt(valueStr));
                    break;
                case 5: // Long
                    nbt.putLong(key, Long.parseLong(valueStr));
                    break;
                case 6: // Float
                    nbt.putFloat(key, Float.parseFloat(valueStr));
                    break;
                case 7: // Double
                    nbt.putDouble(key, Double.parseDouble(valueStr));
                    break;
                case 8: // String
                    nbt.putString(key, valueStr);
                    break;
                default:
                    nbt.putString(key, valueStr);
            }
        } catch (NumberFormatException e) {
            HackerMod.LOGGER.error("Failed to parse value for key {}: {}", key, valueStr);
        }
    }

    private static void setNbtValueFromString(NbtCompound nbt, String key, String valueStr) {
        if (nbt.contains(key)) {
            byte type = nbt.getType(key);
            try {
                switch (type) {
                    case 1: nbt.putBoolean(key, Boolean.parseBoolean(valueStr)); break;
                    case 2: nbt.putByte(key, Byte.parseByte(valueStr)); break;
                    case 3: nbt.putShort(key, Short.parseShort(valueStr)); break;
                    case 4: nbt.putInt(key, Integer.parseInt(valueStr)); break;
                    case 5: nbt.putLong(key, Long.parseLong(valueStr)); break;
                    case 6: nbt.putFloat(key, Float.parseFloat(valueStr)); break;
                    case 7: nbt.putDouble(key, Double.parseDouble(valueStr)); break;
                    default: nbt.putString(key, valueStr);
                }
            } catch (NumberFormatException e) {
                nbt.putString(key, valueStr);
            }
        } else {
            nbt.putString(key, valueStr);
        }
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
