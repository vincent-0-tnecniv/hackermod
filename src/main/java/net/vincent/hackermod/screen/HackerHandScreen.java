package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.BlockUpdatePacket;

import java.util.ArrayList;
import java.util.List;

public class HackerHandScreen extends Screen {
    public static final Identifier GUI_TEXTURE =
            Identifier.of(HackerMod.MOD_ID, "textures/gui/hacker_hand/hacker_hand_gui_block.png");

    private final BlockPos blockPos;
    private final BlockState blockState;
    private final List<PropertyEditor> propertyEditors = new ArrayList<>();

    private int nextY = 80; // Track Y position for each property

    public HackerHandScreen(BlockPos pos, BlockState state) {
        super(Text.literal("Block Editor - " + state.getBlock().getName().getString()));
        this.blockPos = pos;
        this.blockState = state;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Reset Y position
        nextY = 100;

        // Create editor for each property
        for (Property<?> property : blockState.getProperties()) {
            PropertyEditor editor = new PropertyEditor(property, nextY);
            propertyEditors.add(editor);
            nextY += 30; // Space between each property
        }
        // This will have to change in the future - this will be made so that a button, when clicked, cycles through the items
        // i.e. using an iterating i to switch through the blockstates

        // Confirm button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Confirm"),
                button -> {
                    for (PropertyEditor editor : propertyEditors) {
                        if (editor.hasChanges()) {
                            ClientPlayNetworking.send(new BlockUpdatePacket(
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

    // Inner class to handle each property
    private class PropertyEditor {
        private final Property<?> property;
        private final TextWidget labelWidget;
        private final TextFieldWidget valueField;
        private String originalValue;
        private String newValue;

        @SuppressWarnings({"rawtypes", "unchecked"})
        public PropertyEditor(Property property, int yPos) {
            this.property = property;
            this.originalValue = blockState.get(property).toString();
            this.newValue = this.originalValue;

            int centerX = HackerHandScreen.this.width / 2;
            int labelWidth = 100;
            int fieldWidth = 100;
            int spacing = 10;

            // Create label (left side)
            this.labelWidget = new TextWidget(
                    centerX - labelWidth - spacing,
                    yPos,
                    labelWidth,
                    20,
                    Text.literal(property.getName() + ":"),
                    HackerHandScreen.this.textRenderer
            );
            this.labelWidget.setTextColor(0x00FF00); // Neon green color

            // Create text field (right side)
            this.valueField = new TextFieldWidget(
                    HackerHandScreen.this.textRenderer,
                    centerX + spacing,
                    yPos,
                    fieldWidth,
                    20,
                    Text.literal("If you see this text, please report as a bug!")
            );
            this.valueField.setText(this.originalValue);
            this.valueField.setPlaceholder(Text.literal("Current: " + this.originalValue));
            this.valueField.setChangedListener(text -> {
                this.newValue = text;
            });

            // Add to screen
            HackerHandScreen.this.addDrawableChild(this.labelWidget);
            HackerHandScreen.this.addDrawableChild(this.valueField);
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

        // Draw header line
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Info"),
                centerX,
                35,
                0xFFFFFF
        );

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Block: " + blockState.getBlock().getName().getString()),
                centerX,
                55,
                0x00FF00
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Position: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()),
                centerX,
                75,
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
        // Let each text field handle its own key presses
        for (PropertyEditor editor : propertyEditors) {
            if (editor.valueField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
