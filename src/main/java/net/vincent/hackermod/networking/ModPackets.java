package net.vincent.hackermod.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
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

        PayloadTypeRegistry.playC2S().register(EntityUpdatePacket.ID, EntityUpdatePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntityUpdatePacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                var entity = world.getEntityById(packet.entityId());

                if (entity != null) {
                    // Get current NBT
                    NbtCompound nbt = new NbtCompound();
                    entity.saveNbt(nbt);

                    // Apply the change
                    setNbtValueFromString(nbt, packet.nbtPath(), packet.nbtValue());

                    // Write back to entity
                    entity.readNbt(nbt);

                    context.player().sendMessage(
                            Text.literal("§aUpdated " + packet.nbtPath() + " to " + packet.nbtValue()),
                            true
                    );

                    HackerMod.LOGGER.info("Entity {} updated: {} = {}",
                            entity.getId(), packet.nbtPath(), packet.nbtValue());
                }
            });
        });

        HackerMod.LOGGER.info("Registering Packets for " + HackerMod.MOD_ID);
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
