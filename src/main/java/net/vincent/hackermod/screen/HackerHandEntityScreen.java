package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.EntityUpdatePacket;

import java.util.ArrayList;
import java.util.List;

public class HackerHandEntityScreen extends Screen {
    // TODO: fix this EXTREMELY buggy screen
    private final Entity targetEntity;
    private final List<NbtEditor> nbtEditors = new ArrayList<>();
    private TextFieldWidget searchField;
    private int scrollOffset = 0;
    private NbtCompound currentNbt;

    public HackerHandEntityScreen(Entity entity) {
        super(Text.literal("Entity Editor - " + entity.getName().getString()));
        this.targetEntity = entity;
        refreshNbtData();
    }

    private void refreshNbtData() {
        this.currentNbt = new NbtCompound();
        targetEntity.saveNbt(this.currentNbt);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Search field
        TextWidget searchLabel = new TextWidget(
                centerX - 150,
                55,
                50,
                20,
                Text.literal("Search:"),
                this.textRenderer
        );
        searchLabel.setTextColor(0x00AAFF);

        this.searchField = new TextFieldWidget(
                this.textRenderer,
                centerX - 100,
                55,
                200,
                20,
                Text.literal("Search")
        );
        this.searchField.setPlaceholder(Text.literal("Filter NBT keys..."));
        this.searchField.setChangedListener(text -> filterNbtEditors(text));

        // Scroll buttons
        ButtonWidget scrollUp = ButtonWidget.builder(
                Text.literal("▲"),
                button -> {
                    if (scrollOffset > 0) {
                        scrollOffset--;
                        recreateEditors();
                    }
                }
        ).dimensions(centerX + 160, 80, 20, 20).build();

        ButtonWidget scrollDown = ButtonWidget.builder(
                Text.literal("▼"),
                button -> {
                    scrollOffset++;
                    recreateEditors();
                }
        ).dimensions(centerX + 160, 100, 20, 20).build();

        // Refresh button - reload current values from entity
        ButtonWidget refreshButton = ButtonWidget.builder(
                Text.literal("⟳"),
                button -> {
                    refreshNbtData();
                    recreateEditors();
                }
        ).dimensions(centerX + 120, 55, 20, 20).build();

        // Confirm button
        ButtonWidget confirmButton = ButtonWidget.builder(
                Text.literal("Save Changes"),
                button -> saveChanges()
        ).dimensions(centerX - 105, centerY + 50, 100, 20).build();

        ButtonWidget cancelButton = ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> this.close()
        ).dimensions(centerX + 5, centerY + 50, 100, 20).build();

        this.addDrawableChild(searchLabel);
        this.addDrawableChild(this.searchField);
        this.addDrawableChild(refreshButton);
        this.addDrawableChild(scrollUp);
        this.addDrawableChild(scrollDown);
        this.addDrawableChild(confirmButton);
        this.addDrawableChild(cancelButton);

        recreateEditors();
    }

    private void recreateEditors() {
        // Clear existing
        for (NbtEditor editor : nbtEditors) {
            editor.setVisible(false);
        }
        nbtEditors.clear();

        // Get fresh NBT
        refreshNbtData();

        int yOffset = 90;
        int visibleCount = 0;
        List<String> keys = new ArrayList<>(currentNbt.getKeys());

        // Sort keys alphabetically for consistency
        keys.sort(String::compareToIgnoreCase);

        for (String key : keys) {
            // Skip if filtered
            if (!searchField.getText().isEmpty()) {
                if (!key.toLowerCase().contains(searchField.getText().toLowerCase())) {
                    continue;
                }
            }

            // Skip if scrolled
            if (visibleCount < scrollOffset) {
                visibleCount++;
                continue;
            }

            // Limit to 12 per page
            if (visibleCount - scrollOffset >= 12) {
                visibleCount++;
                continue;
            }

            Object value = getNbtValue(currentNbt, key);
            String valueStr = formatValue(value);
            boolean isCompound = currentNbt.getType(key) == 10;

            NbtEditor editor = new NbtEditor(key, valueStr, yOffset, isCompound);
            nbtEditors.add(editor);
            yOffset += 35;
            visibleCount++;
        }
    }

    private String formatValue(Object value) {
        String str = value.toString();
        if (str.length() > 35) {
            return str.substring(0, 32) + "...";
        }
        return str;
    }

    private Object getNbtValue(NbtCompound nbt, String key) {
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
            case 9: return nbt.getList(key, 0);
            case 10: return nbt.getCompound(key);
            case 11: return nbt.getIntArray(key);
            default: return "?";
        }
    }

    private void filterNbtEditors(String searchText) {
        for (NbtEditor editor : nbtEditors) {
            boolean visible = searchText.isEmpty() ||
                    editor.getKey().toLowerCase().contains(searchText.toLowerCase());
            editor.setVisible(visible);
        }
    }

    private void saveChanges() {
        NbtCompound modifiedNbt = new NbtCompound();
        targetEntity.saveNbt(modifiedNbt);

        boolean hasChanges = false;

        for (NbtEditor editor : nbtEditors) {
            if (editor.hasChanges()) {
                hasChanges = true;
                String newValue = editor.getNewValue();
                String key = editor.getKey();

                // Set the value in the NBT
                setNbtValueFromString(modifiedNbt, key, newValue);

                ClientPlayNetworking.send(new EntityUpdatePacket(
                        targetEntity.getId(),
                        key,
                        newValue
                ));
                HackerMod.LOGGER.info("Updating {} to {}", key, newValue);
            }
        }

        if (!hasChanges) {
            HackerMod.LOGGER.info("No changes to save");
        }

        this.close();
    }

    private void setNbtValueFromString(NbtCompound nbt, String key, String valueStr) {
        // Try to preserve the original type
        if (nbt.contains(key)) {
            byte type = nbt.getType(key);
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
        } else {
            nbt.putString(key, valueStr);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Editing: " + targetEntity.getName().getString()),
                centerX,
                25,
                0x00FF00
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("UUID: " + targetEntity.getUuid().toString().substring(0, 8) + "..."),
                centerX,
                40,
                0x888888
        );

        // Show count of visible NBT entries
        int visibleCount = 0;
        for (NbtEditor editor : nbtEditors) {
            if (editor.isVisible()) visibleCount++;
        }
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Showing " + visibleCount + " NBT keys"),
                centerX,
                70,
                0x666666
        );
    }

    private void renderCustomBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xB00A0A2A);
        context.drawBorder(2, 2, this.width - 4, this.height - 4, 0xFF00FF66);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Prevent default background
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    // NBT Editor Inner Class
    private class NbtEditor {
        private final String key;
        private final TextFieldWidget valueField;
        private final String originalValue;
        private String newValue;
        private TextWidget labelWidget;
        private TextWidget currentValueWidget;
        private boolean hasChanges = false;
        private boolean visible = true;

        public NbtEditor(String key, String currentValue, int yPos, boolean isCompound) {
            this.key = key;
            this.originalValue = currentValue;
            this.newValue = currentValue;

            int centerX = HackerHandEntityScreen.this.width / 2;
            int labelWidth = 130;
            int currentWidth = 130;
            int fieldWidth = 100;

            // Label
            this.labelWidget = new TextWidget(
                    centerX - labelWidth - 5,
                    yPos,
                    labelWidth,
                    20,
                    Text.literal(key + ":"),
                    HackerHandEntityScreen.this.textRenderer
            );
            this.labelWidget.setTextColor(isCompound ? 0xFFFF66 : 0x00AAFF);

            // Current value display (shows the ACTUAL current value from fresh NBT)
            // Get fresh value directly from entity
            NbtCompound freshNbt = new NbtCompound();
            targetEntity.saveNbt(freshNbt);
            String freshValue = formatValue(getNbtValue(freshNbt, key));
            this.currentValueWidget = new TextWidget(
                    centerX + 5,
                    yPos,
                    currentWidth,
                    20,
                    Text.literal(freshValue),
                    HackerHandEntityScreen.this.textRenderer
            );
            this.currentValueWidget.setTextColor(0x88FF88);

            // Text field
            this.valueField = new TextFieldWidget(
                    HackerHandEntityScreen.this.textRenderer,
                    centerX + currentWidth + 15,
                    yPos,
                    fieldWidth,
                    20,
                    Text.literal("New value")
            );
            this.valueField.setText("");
            this.valueField.setPlaceholder(Text.literal("New value"));
            this.valueField.setEditable(!isCompound);
            this.valueField.setChangedListener(text -> {
                if (!text.isEmpty() && !text.equals(originalValue)) {
                    this.newValue = text;
                    this.hasChanges = true;
                    labelWidget.setTextColor(0xFFFF00);
                    currentValueWidget.setTextColor(0xFFFF00);
                } else {
                    this.newValue = originalValue;
                    this.hasChanges = false;
                    labelWidget.setTextColor(isCompound ? 0xFFFF66 : 0x00AAFF);
                    currentValueWidget.setTextColor(0x88FF88);
                }
            });

            HackerHandEntityScreen.this.addDrawableChild(labelWidget);
            HackerHandEntityScreen.this.addDrawableChild(currentValueWidget);
            HackerHandEntityScreen.this.addDrawableChild(valueField);
        }

        public String getKey() { return key; }
        public String getNewValue() { return newValue; }
        public boolean hasChanges() { return hasChanges; }
        public boolean isVisible() { return visible; }

        public void setVisible(boolean visible) {
            this.visible = visible;
            labelWidget.visible = visible;
            currentValueWidget.visible = visible;
            valueField.visible = visible;
        }
    }
}