package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.BlockStateUpdatePacket;
import net.vincent.hackermod.networking.BlockUpdatePacket;

import java.util.ArrayList;
import java.util.List;

public class HackerHandBlockScreen extends Screen {

    private final BlockPos blockPos;
    private final BlockState blockState;
    private final List<PropertyEditor> propertyEditors = new ArrayList<>();

    // Block ID editing
    private TextFieldWidget blockIdField;
    private String originalBlockId;
    private String newBlockId;

    public HackerHandBlockScreen(BlockPos pos, BlockState state) {
        super(Text.literal("Block Editor - " + state.getBlock().getName().getString()));
        this.blockPos = pos;
        this.blockState = state;
        this.originalBlockId = Registries.BLOCK.getId(state.getBlock()).getPath();
        this.newBlockId = this.originalBlockId;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Block ID editor (top of screen)
        TextWidget blockIdLabel = new TextWidget(
                centerX - 100,
                55,
                80,
                20,
                Text.literal("Block ID:"),
                this.textRenderer
        );
        blockIdLabel.setTextColor(0x00FF00);

        this.blockIdField = new TextFieldWidget(
                this.textRenderer,
                centerX - 10,
                55,
                150,
                20,
                Text.literal("Enter block ID")
        );
        this.blockIdField.setText(this.originalBlockId);
        this.blockIdField.setPlaceholder(Text.literal(this.originalBlockId));
        this.blockIdField.setChangedListener(text -> {
            this.newBlockId = text;
        });

        this.addDrawableChild(blockIdLabel);
        this.addDrawableChild(this.blockIdField);

        // Create property editors for each property
        int yOffset = 100;
        for (Property<?> property : blockState.getProperties()) {
            PropertyEditor editor = new PropertyEditor(property, yOffset, centerX);
            propertyEditors.add(editor);
            yOffset += 35;
        }

        // Confirm button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Confirm"),
                button -> {
                    // Handle Block ID change
                    if (hasBlockIdChanges()) {
                        ClientPlayNetworking.send(new BlockUpdatePacket(blockPos, newBlockId));
                        HackerMod.LOGGER.info("Changing block to: {}", newBlockId);
                    }

                    // Handle property changes
                    for (PropertyEditor editor : propertyEditors) {
                        if (editor.hasChanges()) {
                            ClientPlayNetworking.send(new BlockStateUpdatePacket(
                                    blockPos,
                                    editor.getPropertyName(),
                                    editor.getNewValue()
                            ));
                            HackerMod.LOGGER.info("Updating {} to {}", editor.getPropertyName(), editor.getNewValue());
                        }
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

    private boolean hasBlockIdChanges() {
        return !newBlockId.equals(originalBlockId);
    }

    // Inner class for editing each property
    private class PropertyEditor {
        private final Property<?> property;
        private final TextFieldWidget valueField;
        private final String originalValue;
        private String newValue;

        @SuppressWarnings({"rawtypes", "unchecked"})
        public PropertyEditor(Property property, int yPos, int centerX) {
            this.property = property;
            this.originalValue = blockState.get(property).toString();
            this.newValue = this.originalValue;

            int labelWidth = 80;
            int fieldWidth = 120;
            int spacing = 10;
            int startX = centerX - (labelWidth + fieldWidth + spacing) / 2;

            // Property label
            TextWidget labelWidget = new TextWidget(
                    startX,
                    yPos,
                    labelWidth,
                    20,
                    Text.literal(property.getName() + ":"),
                    HackerHandBlockScreen.this.textRenderer
            );
            labelWidget.setTextColor(0x00FF00);

            // Property value text field (EDITABLE)
            this.valueField = new TextFieldWidget(
                    HackerHandBlockScreen.this.textRenderer,
                    startX + labelWidth + spacing,
                    yPos,
                    fieldWidth,
                    20,
                    Text.literal("Enter value")
            );
            this.valueField.setText(this.originalValue);
            this.valueField.setPlaceholder(Text.literal("Current: " + this.originalValue));
            this.valueField.setChangedListener(text -> {
                this.newValue = text;
            });

            // Show valid values hint
            String hint;
            if (property.getType() == Boolean.class) {
                hint = "true/false";
            } else {
                hint = property.getValues().toString();
            }

            TextWidget hintWidget = new TextWidget(
                    startX + labelWidth + spacing + fieldWidth + 5,
                    yPos,
                    150,
                    20,
                    Text.literal("Valid: " + hint),
                    HackerHandBlockScreen.this.textRenderer
            );
            hintWidget.setTextColor(0x888888);

            HackerHandBlockScreen.this.addDrawableChild(labelWidget);
            HackerHandBlockScreen.this.addDrawableChild(this.valueField);
            HackerHandBlockScreen.this.addDrawableChild(hintWidget);
        }

        public String getPropertyName() {
            return property.getName();
        }

        public String getNewValue() {
            return newValue;
        }

        public boolean hasChanges() {
            return !newValue.equals(originalValue);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // Draw header
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Block Editor"),
                centerX,
                30,
                0x00FF00
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Position: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()),
                centerX,
                45,
                0x00AAFF
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
        // Let text fields handle their own key presses
        if (this.blockIdField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (PropertyEditor editor : propertyEditors) {
            if (editor.valueField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}