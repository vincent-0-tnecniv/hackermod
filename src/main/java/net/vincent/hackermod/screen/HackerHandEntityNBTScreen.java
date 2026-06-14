package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.EntityNbtPacket;
import net.vincent.hackermod.networking.EntityTeleportPacket;
import net.vincent.hackermod.networking.EntityTransformPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HackerHandEntityNBTScreen extends Screen {

    private Vec3d entityPos;
    private NbtCompound entityNbt;
    private final World world;
    private Entity entityEdited;
    private TextFieldWidget entityIdField, xField, yField, zField;
    private final String originalEntityId;
    private String newEntityId;
    private final double ox, oy, oz;
    private double nx, ny, nz;

    private final List<NbtEditor> nbtEditors = new ArrayList<>();

    public int centerX;
    public int centerY;

    public HackerHandEntityNBTScreen(Entity entity, World world) {
        super(Text.literal("Entity Editor - " + entity.getName().getString()));
        this.entityEdited = entity;
        this.entityNbt = new NbtCompound();
        entityEdited.writeNbt(this.entityNbt);
        this.originalEntityId = Registries.ENTITY_TYPE.getId(entity.getType()).getPath();
        this.world = world;
        this.entityPos = entity.getPos();
        this.ox = round(entityPos.getX(), 2);
        this.oy = round(entityPos.getY(), 2);
        this.oz = round(entityPos.getZ(), 2);
        this.nx = this.ox;
        this.ny = this.oy;
        this.nz = this.oz;
        this.newEntityId = this.originalEntityId;
    }

    @Override
    protected void init() {
        super.init();

        centerX = this.width / 2;
        centerY = this.height / 2;

        // Entity ID editor
        TextWidget entityIdLabel = new TextWidget(
                centerX - 100,
                55,
                80,
                20,
                Text.literal("Entity ID: "),
                this.textRenderer
        );
        entityIdLabel.setTextColor(0x00AAFF);

        this.entityIdField = new TextFieldWidget(
                this.textRenderer,
                centerX - 10,
                55,
                150,
                20,
                Text.literal("Enter entity ID")
        );
        this.entityIdField.setText(this.originalEntityId);
        this.entityIdField.setPlaceholder(Text.literal(this.originalEntityId));
        this.entityIdField.setChangedListener(text -> this.newEntityId = text);

        this.addDrawableChild(entityIdLabel);
        this.addDrawableChild(this.entityIdField);

        // Position editor
        TextWidget positionLabel = new TextWidget(
                centerX - 100,
                75,
                80,
                20,
                Text.literal("Position: "),
                this.textRenderer
        );
        positionLabel.setTextColor(0x00AAFF);

        this.xField = new TextFieldWidget(
                this.textRenderer,
                centerX - 10,
                75,
                40,
                20,
                Text.literal(String.valueOf(ox))
        );
        this.xField.setText(String.valueOf(this.ox));
        this.xField.setPlaceholder(Text.of(String.valueOf(this.ox)));
        this.xField.setChangedListener(text -> {
            try {
                this.nx = Double.parseDouble(text);
            } catch (Exception e) {
                this.nx = this.ox;
            }
        });

        this.yField = new TextFieldWidget(
                this.textRenderer,
                centerX + 30,
                75,
                40,
                20,
                Text.literal(String.valueOf(oy))
        );
        this.yField.setText(String.valueOf(this.oy));
        this.yField.setPlaceholder(Text.of(String.valueOf(this.oy)));
        this.yField.setChangedListener(text -> {
            try {
                this.ny = Double.parseDouble(text);
            } catch (Exception e) {
                this.ny = this.oy;
            }
        });

        this.zField = new TextFieldWidget(
                this.textRenderer,
                centerX + 70,
                75,
                40,
                20,
                Text.literal(String.valueOf(oz))
        );
        this.zField.setText(String.valueOf(this.oz));
        this.zField.setPlaceholder(Text.of(String.valueOf(this.oz)));
        this.zField.setChangedListener(text -> {
            try {
                this.nz = Double.parseDouble(text);
            } catch (Exception e) {
                this.nz = this.oz;
            }
        });

        this.addDrawableChild(positionLabel);
        this.addDrawableChild(this.xField);
        this.addDrawableChild(this.yField);
        this.addDrawableChild(this.zField);

        // Create NBT editors
        int yOffset = 110;
        if (!(entityEdited instanceof PlayerEntity)) {
            for (String key : entityNbt.getKeys()) {
                byte type = entityNbt.getType(key);
                Object value = getNbtValue(entityNbt, key);

                // Skip arrays and complex types for editing (display only)
                boolean isEditable = isTypeEditable(type);
                NbtEditor editor = new NbtEditor(key, value, type, yOffset, isEditable);
                nbtEditors.add(editor);
                yOffset += 35;
                HackerMod.LOGGER.info("{}: {} (type: {}, editable: {})", key, value, type, isEditable);
            }
        }

        // Confirm button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Confirm"),
                button -> {
                    // Handle Entity Type Change
                    if (hasEntityIdChanges()) {
                        String finalEntityId = newEntityId.contains(":") ? newEntityId : "minecraft:" + newEntityId;
                        double finalX = hasXChanges() ? nx : entityEdited.getX();
                        double finalY = hasYChanges() ? ny : entityEdited.getY();
                        double finalZ = hasZChanges() ? nz : entityEdited.getZ();

                        ClientPlayNetworking.send(new EntityTransformPacket(
                                entityEdited.getId(),
                                finalEntityId,
                                finalX, finalY, finalZ,
                                entityEdited.getYaw(),
                                entityEdited.getPitch()
                        ));
                    }
                    // Handle Position change
                    else if (hasXChanges() || hasYChanges() || hasZChanges()) {
                        ClientPlayNetworking.send(new EntityTeleportPacket(entityEdited.getId(), nx, ny, nz));
                        HackerMod.LOGGER.info("Moving entity to: {},{},{}", nx, ny, nz);
                    }

                    // Handle NBT changes
                    boolean hasNbtChanges = false;
                    for (NbtEditor editor : nbtEditors) {
                        if (editor.hasChanges()) {
                            ClientPlayNetworking.send(new EntityNbtPacket(
                                    entityEdited.getId(),
                                    editor.getName(),
                                    editor.getValueAsString(),
                                    editor.getDataType()
                            ));
                            hasNbtChanges = true;
                        }
                    }

                    if (hasNbtChanges) {
                        client.execute(() -> {
                            try {
                                Thread.sleep(100); // Small delay
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            refreshEntityData();
                        });
                    }

                    this.close();
                }
        ).dimensions(centerX + 10, centerY + 50, 100, 20).build());

        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> this.close()
        ).dimensions(centerX - 110, centerY + 50, 100, 20).build());
    }

    public void refreshEntityData() {
        if (entityEdited == null) return;

        // Re-read NBT from the (now updated) client entity
        this.entityNbt = new NbtCompound();
        entityEdited.writeNbt(this.entityNbt);

        // Update all editor displays
        for (NbtEditor editor : nbtEditors) {
            if (this.entityNbt.contains(editor.getName())) {
                byte type = this.entityNbt.getType(editor.getName());
                Object currentValue = getNbtValue(this.entityNbt, editor.getName());
                String displayValue = formatValueForDisplay(currentValue, type);

                editor.valueField.setText(displayValue);
                editor.valueField.setPlaceholder(Text.literal("Current: " + displayValue));
            }
        }

        // Update position fields
        Vec3d currentPos = entityEdited.getPos();
        this.nx = currentPos.x;
        this.ny = currentPos.y;
        this.nz = currentPos.z;
        this.xField.setText(String.valueOf(round(this.nx, 2)));
        this.yField.setText(String.valueOf(round(this.ny, 2)));
        this.zField.setText(String.valueOf(round(this.nz, 2)));
    }

    private boolean isTypeEditable(byte type) {
        // Only primitive types and strings are editable via text field
        // Arrays, Lists, and Compounds are read-only
        return type >= 1 && type <= 8 && type != 9 && type != 10 && type != 11 && type != 12;
    }

    private Object getNbtValue(NbtCompound nbt, String key) {
        if (nbt == null) {
            HackerMod.LOGGER.warn("getNbtValue called with null NBT");
            return "NBT is null";
        }

        if (!nbt.contains(key)) {
            return "Key not found: " + key;
        }

        try {
            byte type = nbt.getType(key);
            switch (type) {
                case 1: return nbt.getBoolean(key);
                case 2: return nbt.getByte(key);
                case 3: return nbt.getShort(key);
                case 4: return nbt.getInt(key);
                case 5: return nbt.getLong(key);
                case 6: return nbt.getFloat(key);
                case 7: return nbt.getDouble(key);
                case 8: return nbt.getString(key);
                case 9:
                    NbtList list = nbt.getList(key, 0);
                    return list == null ? "[]" : list;
                case 10:
                    NbtCompound compound = nbt.getCompound(key);
                    return compound == null ? "{}" : compound;
                case 11:
                    int[] intArray = nbt.getIntArray(key);
                    return intArray == null ? new int[0] : intArray;
                case 12:
                    long[] longArray = nbt.getLongArray(key);
                    return longArray == null ? new long[0] : longArray;
                default:
                    return "Unknown type: " + type;
            }
        } catch (Exception e) {
            HackerMod.LOGGER.error("Error reading NBT key '{}': {}", key, e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String formatValueForDisplay(Object value, byte type) {
        if (value == null) return "null";

        try {
            switch (type) {
                case 1: // Boolean
                case 2: // Byte
                case 3: // Short
                case 4: // Int
                case 5: // Long
                case 6: // Float
                case 7: // Double
                    return value.toString();
                case 8: // String
                    return (String) value;
                case 9: // List
                    if (value instanceof NbtList list) {
                        if (list.isEmpty()) return "[] (read-only)";
                        return String.format("[%d elements - read-only]", list.size());
                    }
                    return "[List - read-only]";
                case 10: // Compound
                    if (value instanceof NbtCompound compound) {
                        if (compound.isEmpty()) return "{} (read-only)";
                        return String.format("{%d entries - read-only}", compound.getKeys().size());
                    }
                    return "{Compound - read-only}";
                case 11: // INT_ARRAY
                    if (value instanceof int[]) {
                        int[] arr = (int[]) value;
                        if (arr.length == 0) return "[] (read-only)";
                        if (arr.length > 5) {
                            return String.format("[%d ints - read-only]", arr.length);
                        }
                        return Arrays.toString(arr) + " (read-only)";
                    }
                    return "[IntArray - read-only]";
                case 12: // LONG_ARRAY
                    if (value instanceof long[]) {
                        long[] arr = (long[]) value;
                        if (arr.length == 0) return "[] (read-only)";
                        if (arr.length > 5) {
                            return String.format("[%d longs - read-only]", arr.length);
                        }
                        return Arrays.toString(arr) + " (read-only)";
                    }
                    return "[LongArray - read-only]";
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private double round(double value, int digits) {
        return Math.round((float) (value * Math.pow(10, digits))) / Math.pow(10, digits);
    }

    private boolean hasEntityIdChanges() {
        return !newEntityId.equals(originalEntityId);
    }

    private boolean hasZChanges() {
        return nz != oz;
    }

    private boolean hasYChanges() {
        return ny != oy;
    }

    private boolean hasXChanges() {
        return nx != ox;
    }

    private class NbtEditor {
        private final TextFieldWidget valueField;
        private final String key;
        private final Object oldValue;
        private String newValue;
        private final byte dataType;
        private final boolean editable;

        public NbtEditor(String key, Object value, byte type, int yPos, boolean editable) {
            this.key = key;
            this.oldValue = value;
            this.newValue = formatValueForDisplay(value, type);
            this.dataType = type;
            this.editable = editable;

            int labelWidth = 80;
            int fieldWidth = 200;
            int spacing = 10;
            int startX = centerX - (labelWidth + fieldWidth + spacing) / 2;

            // Label
            TextWidget labelWidget = new TextWidget(
                    startX,
                    yPos,
                    labelWidth,
                    20,
                    Text.literal(this.key + ":"),
                    HackerHandEntityNBTScreen.this.textRenderer
            );
            labelWidget.setTextColor(editable ? 0x00FF00 : 0xFFAA00);

            // Value field
            String displayValue = formatValueForDisplay(value, type);
            this.valueField = new TextFieldWidget(
                    HackerHandEntityNBTScreen.this.textRenderer,
                    startX + labelWidth + spacing,
                    yPos,
                    fieldWidth,
                    20,
                    Text.literal("Enter value")
            );
            this.valueField.setText(displayValue);
            this.valueField.setEditable(editable);

            if (editable) {
                this.valueField.setPlaceholder(Text.literal("Current: " + displayValue));
                this.valueField.setChangedListener(text -> this.newValue = text);
            } else {
                this.valueField.setPlaceholder(Text.literal("Read-only value"));
            }

            HackerHandEntityNBTScreen.this.addDrawableChild(labelWidget);
            HackerHandEntityNBTScreen.this.addDrawableChild(this.valueField);
        }

        public String getName() {
            return key;
        }

        public String getValueAsString() {
            return newValue;
        }

        public byte getDataType() {
            return dataType;
        }

        public boolean hasChanges() {
            if (!editable) return false;
            String currentDisplay = formatValueForDisplay(oldValue, dataType);
            return !newValue.equals(currentDisplay) && !newValue.isEmpty();
        }

        public void refreshValue(NbtCompound freshNbt) {
            if (freshNbt.contains(key)) {
                Object currentValue = getNbtValue(freshNbt, key);
                String displayValue = formatValueForDisplay(currentValue, dataType);
                this.valueField.setText(displayValue);
                this.newValue = displayValue;

                // Also update the placeholder
                this.valueField.setPlaceholder(Text.literal("Current: " + displayValue));

                HackerMod.LOGGER.info("Refreshed {} to: {}", key, displayValue);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Entity Editor"),
                centerX,
                30,
                0x00FF00
        );

        // Show hint about read-only values
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Yellow labels = read-only (arrays, lists, compounds)"),
                centerX,
                this.height - 20,
                0x888888
        );
    }

    private void renderCustomBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xB00A0A2A);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Prevent default background
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.entityIdField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (this.xField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (this.yField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (this.zField.keyPressed(keyCode, scanCode, modifiers)) return true;

        for (NbtEditor editor : nbtEditors) {
            if (editor.valueField.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}