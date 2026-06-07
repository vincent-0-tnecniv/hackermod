package net.vincent.hackermod.screen;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public class HackerHandScreen extends Screen {
    public static final Identifier GUI_TEXTURE =
            Identifier.of(HackerMod.MOD_ID, "textures/gui/hacker_hand/hacker_hand_gui_block.png");

    private final BlockPos blockPos;
    private final BlockState blockState;

    public int centerX;
    public int centerY;

    // Text field for editing
    private TextFieldWidget propertyEditorField;
    private String currentPropertyValue;

    public HackerHandScreen(BlockPos pos, BlockState state) {
        super(Text.literal("Block Editor - " + state.getBlock().getName().getString()));
        this.blockPos = pos;
        this.blockState = state;
    }

    @Override
    protected void init() {
        super.init();

        centerX = this.width / 2;
        centerY = this.height / 2;

        // Create the text field
        this.propertyEditorField = new TextFieldWidget(
                this.textRenderer,           // Text renderer
                centerX - 100,               // X position
                centerY - 20,                // Y position
                200,                         // Width
                20,                          // Height
                Text.literal("Edit Property") // Label
        );

        // Set initial text (show first property as example)
        if (!blockState.getProperties().isEmpty()) {
            Property<?> firstProperty = blockState.getProperties().iterator().next();
            Object value = blockState.get(firstProperty);
            this.currentPropertyValue = value.toString();
            this.propertyEditorField.setText(this.currentPropertyValue);
        } else {
            this.propertyEditorField.setText("No editable properties");
            this.propertyEditorField.setEditable(false);
        }

        // Optional: Add a hint text when empty
        this.propertyEditorField.setPlaceholder(Text.literal("Enter new value..."));

        // Set max length
        this.propertyEditorField.setMaxLength(50);

        // Add change listener (optional - for live preview)
        this.propertyEditorField.setChangedListener(text -> {
            this.currentPropertyValue = text;
            HackerMod.LOGGER.info("Property value changed to: " + text);
        });

        // Add the text field to the screen
        this.addDrawableChild(this.propertyEditorField);

        // Close button (you'll replace with confirm later)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(centerX - 50, centerY + 50, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderCustomBackground(context);

        super.render(context, mouseX, mouseY, delta);

        // Display block info
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Block: " + blockState.getBlock().getName().getString()),
                this.width / 2,
                40,
                0x00FF00
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Position: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()),
                this.width / 2,
                60,
                0x00AAFF
        );

        // Display which property we're editing
        if (!blockState.getProperties().isEmpty()) {
            Property<?> firstProperty = blockState.getProperties().iterator().next();
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Editing: " + firstProperty.getName()),
                    this.width / 2,
                    centerY - 45,
                    0x88FF88
            );
        }

        // Draw label for text field
        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("Value:"),
                this.width / 2 - 100,
                centerY - 35,
                0xAAAAAA
        );
    }

    private void renderCustomBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xB00A0A2A);
        int borderColor = 0xFF00FF66;
        context.drawBorder(2, 2, this.width - 4, this.height - 4, borderColor);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override to prevent default dark background
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Let the text field handle its own key presses first
        if (this.propertyEditorField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}