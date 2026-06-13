package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.EntityNBTPacket;
import net.vincent.hackermod.networking.EntityTransformPacket;

public class HackerHandEntityNBTScreen extends Screen {

    private Vec3d entityPos;
    private final World world;
    private Entity entityEdited;
    private TextFieldWidget entityIdField, xField, yField, zField;
    private final String originalEntityId;
    private String newEntityId;
    private final double ox, oy, oz;
    private double nx, ny, nz;

    public HackerHandEntityNBTScreen(Entity entity, World world) {
        super(Text.literal("Entity Editor - " + entity.getName().getString()));
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
        this.entityEdited = entity;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Entity ID editor - this is not an actual editor, but a "discard and replace"
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
                Text.literal(String.valueOf(ox))
        );
        this.xField.setText(String.valueOf(this.ox));
        this.xField.setPlaceholder(Text.of(String.valueOf(this.ox)));
        this.xField.setChangedListener(text -> {
            try{
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
            try{
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
            try{
                this.nz = Double.parseDouble(text);
            } catch (Exception e) {
                this.nz = this.oz;
            }
        });

        this.addDrawableChild(positionLabel);
        this.addDrawableChild(this.xField);
        this.addDrawableChild(this.yField);
        this.addDrawableChild(this.zField);

        // TODO: Create NBT editors for each NBT

//        int yOffset = 100;
//        for (Property<?> property : blockState.getProperties()) {
//            PropertyEditor editor = new PropertyEditor(property, yOffset, centerX);
//            propertyEditors.add(editor);
//            yOffset += 35;
//        }

        // Confirm button
        // Confirm button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Confirm"),
                button -> {
                    // Handle Entity Type Change (send packet to server)
                    if (hasEntityIdChanges()) {
                        String finalEntityId = newEntityId.contains(":") ? newEntityId : "minecraft:" + newEntityId;

                        // Use the position from the position fields (or original if not changed)
                        double finalX = hasXChanges() ? nx : entityEdited.getX();
                        double finalY = hasYChanges() ? ny : entityEdited.getY();
                        double finalZ = hasZChanges() ? nz : entityEdited.getZ();

                        // Send transform packet to server
                        ClientPlayNetworking.send(new EntityTransformPacket(
                                entityEdited.getId(),
                                finalEntityId,
                                finalX, finalY, finalZ,
                                entityEdited.getYaw(),
                                entityEdited.getPitch()
                        ));

                        HackerMod.LOGGER.info("Requesting entity transformation to {} at {},{},{}",
                                finalEntityId, finalX, finalY, finalZ);
                    }
                    // Handle Position change only (no entity type change)
                    else if (hasXChanges() || hasYChanges() || hasZChanges()) {
                        ClientPlayNetworking.send(new EntityNBTPacket(entityEdited.getId(), nx, ny, nz));
                        HackerMod.LOGGER.info("Moving entity to: {},{},{}", nx, ny, nz);
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

    private double round(double value, int digits){
        return Math.round((float) (value * Math.pow(10, digits))) / Math.pow(10, digits);
        // just everything compacted into one line - what matters is its use
    }

    private boolean hasEntityIdChanges() {
        return !newEntityId.equals(originalEntityId);
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

    // TODO: rewrite the code for NBT/attribute editor
//    private class PropertyEditor {
//        private final Property<?> property;
//        private final TextFieldWidget valueField;
//        private final String originalValue;
//        private String newValue;
//
//        @SuppressWarnings({"rawtypes", "unchecked"})
//        public PropertyEditor(Property property, int yPos, int centerX) {
//            this.property = property;
//            this.originalValue = blockState.get(property).toString();
//            this.newValue = this.originalValue;
//
//            int labelWidth = 80;
//            int fieldWidth = 120;
//            int spacing = 10;
//            int startX = centerX - (labelWidth + fieldWidth + spacing) / 2;
//
//            // Property label
//            TextWidget labelWidget = new TextWidget(
//                    startX,
//                    yPos,
//                    labelWidth,
//                    20,
//                    Text.literal(property.getName() + ":"),
//                    HackerHandEntityScreen.this.textRenderer
//            );
//            labelWidget.setTextColor(0x00FF00);
//
//            // Property value text field (EDITABLE)
//            this.valueField = new TextFieldWidget(
//                    HackerHandEntityScreen.this.textRenderer,
//                    startX + labelWidth + spacing,
//                    yPos,
//                    fieldWidth,
//                    20,
//                    Text.literal("Enter value")
//            );
//            this.valueField.setText(this.originalValue);
//            this.valueField.setPlaceholder(Text.literal("Current: " + this.originalValue));
//            this.valueField.setChangedListener(text -> this.newValue = text);
//
//            // Show valid values hint
//            String hint;
//            if (property.getType() == Boolean.class) {
//                hint = "true/false";
//            } else {
//                hint = property.getValues().toString();
//            }
//
//            TextWidget hintWidget = new TextWidget(
//                    startX + labelWidth + spacing + fieldWidth + 5,
//                    yPos,
//                    150,
//                    20,
//                    Text.literal("Valid: " + hint),
//                    HackerHandEntityScreen.this.textRenderer
//            );
//            hintWidget.setTextColor(0x888888);
//
//            HackerHandEntityScreen.this.addDrawableChild(labelWidget);
//            HackerHandEntityScreen.this.addDrawableChild(this.valueField);
//            HackerHandEntityScreen.this.addDrawableChild(hintWidget);
//        }
//
//        public String getPropertyName() {
//            return property.getName();
//        }
//
//        public String getNewValue() {
//            return newValue;
//        }
//
//        public boolean hasChanges() {
//            return !newValue.equals(originalValue);
//        }
//    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // Draw header
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Entity Editor"),
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
        if (this.entityIdField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.xField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.yField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.zField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        // TODO: fix the keyPressed()
//        for (PropertyEditor editor : propertyEditors) {
//            if (editor.valueField.keyPressed(keyCode, scanCode, modifiers)) {
//                return true;
//            }
//        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}