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

    private BlockPos blockPos;
    private final BlockState blockState;

    private final List<PropertyEditor> propertyEditors = new ArrayList<>();

    // Block ID editing
    private TextFieldWidget blockIdField;
    private final String originalBlockId;
    private String newBlockId;

    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget zField;
    private final int ox, oy, oz;
    private int nx, ny, nz;

    public HackerHandBlockScreen(BlockPos pos, BlockState state) {
        super(Text.literal("Block Editor - " + state.getBlock().getName().getString()));
        this.blockPos = pos;
        this.blockState = state;
        this.originalBlockId = Registries.BLOCK.getId(state.getBlock()).getPath();
        this.newBlockId = this.originalBlockId;
        this.ox = pos.getX();
        this.oy = pos.getY();
        this.oz = pos.getZ();
        this.nx = this.ox;
        this.ny = this.oy;
        this.nz = this.oz;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Block ID editor
        TextWidget blockIdLabel = new TextWidget(
                centerX - 100,
                55,
                80,
                20,
                Text.literal("Block ID: "),
                this.textRenderer
        );
        blockIdLabel.setTextColor(0x00AAFF);

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
        this.blockIdField.setChangedListener(text -> this.newBlockId = text);

        this.addDrawableChild(blockIdLabel);
        this.addDrawableChild(this.blockIdField);

        // Block Position editor
        TextWidget positionLabel = new TextWidget(
                centerX - 100,
                75,
                80,
                20,
                Text.literal("Position: "),
                this.textRenderer
        );
        positionLabel.setTextColor(0x00AAFF);

        // x, y, and z position fields (they look the same)
        this.xField = new TextFieldWidget(
                this.textRenderer,
                centerX - 10,
                75,
                40,
                20,
                Text.literal(String.valueOf(blockPos.getX()))
        );
        this.xField.setText(String.valueOf(this.ox));
        this.xField.setPlaceholder(Text.of(String.valueOf(this.ox)));
        this.xField.setChangedListener(text -> {
            try{
                this.nx = Integer.parseInt(text);
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
                Text.literal(String.valueOf(blockPos.getY()))
        );
        this.yField.setText(String.valueOf(this.oy));
        this.yField.setPlaceholder(Text.of(String.valueOf(this.oy)));
        this.yField.setChangedListener(text -> {
            try{
                this.ny = Integer.parseInt(text);
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
                Text.literal(String.valueOf(blockPos.getZ()))
        );
        this.zField.setText(String.valueOf(this.oz));
        this.zField.setPlaceholder(Text.of(String.valueOf(this.oz)));
        this.zField.setChangedListener(text -> {
            try{
                this.nz = Integer.parseInt(text);
            } catch (Exception e) {
                this.nz = this.oz;
            }
        });

        this.addDrawableChild(positionLabel);
        this.addDrawableChild(this.xField);
        this.addDrawableChild(this.yField);
        this.addDrawableChild(this.zField);

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

                    // Handle Block Position change
                    if(hasXChanges() || hasYChanges() || hasZChanges()){
                        ClientPlayNetworking.send(new BlockUpdatePacket(blockPos, "minecraft:air"));
                        blockPos = new BlockPos(nx, ny, nz);
                        ClientPlayNetworking.send(new BlockUpdatePacket(blockPos, newBlockId));
                    }

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

    private boolean hasZChanges() {
        return nz!=oz;
    }

    private boolean hasYChanges() {
        return ny!=oy;
    }

    private boolean hasXChanges() {
        return nx!=ox;
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
            this.valueField.setChangedListener(text -> this.newValue = text);

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
        if(this.xField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.yField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.zField.keyPressed(keyCode, scanCode, modifiers)) {
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