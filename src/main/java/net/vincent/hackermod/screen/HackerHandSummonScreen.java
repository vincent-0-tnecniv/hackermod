package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.EntitySummonPacket;

public class HackerHandSummonScreen extends Screen {
    private final BlockPos targetPos;
    private TextFieldWidget entityIdField;

    public HackerHandSummonScreen(BlockPos pos) {
        super(Text.literal("Summon Entity"));
        this.targetPos = pos;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Show target position
        TextWidget posLabel = new TextWidget(
                centerX - 100,
                centerY - 40,
                200,
                20,
                Text.literal("Target: " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ()),
                this.textRenderer
        );
        posLabel.setTextColor(0x00AAFF);

        // Entity ID field
        TextWidget entityLabel = new TextWidget(
                centerX - 100,
                centerY - 10,
                80,
                20,
                Text.literal("Entity ID:"),
                this.textRenderer
        );
        entityLabel.setTextColor(0x00FF00);

        this.entityIdField = new TextFieldWidget(
                this.textRenderer,
                centerX - 10,
                centerY - 10,
                150,
                20,
                Text.literal("")
        );
        this.entityIdField.setPlaceholder(Text.literal(""));
        this.entityIdField.setText("");

        // Summon button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Summon"),
                button -> {
                    String entityId = this.entityIdField.getText();
                    if (!entityId.isEmpty()) {
                        ClientPlayNetworking.send(new EntitySummonPacket(targetPos, entityId));
                        HackerMod.LOGGER.info("Summoning {} at {}", entityId, targetPos);
                    }
                    this.close();
                }
        ).dimensions(centerX - 50, centerY + 30, 100, 20).build());

        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> this.close()
        ).dimensions(centerX + 60, centerY + 30, 100, 20).build());

        this.addDrawableChild(posLabel);
        this.addDrawableChild(entityLabel);
        this.addDrawableChild(this.entityIdField);
    }

    private void renderCustomBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xB00A0A2A);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                0x00FF00
        );
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}