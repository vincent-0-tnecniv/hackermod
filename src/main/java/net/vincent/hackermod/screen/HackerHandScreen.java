package net.vincent.hackermod.screen;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;

public class HackerHandScreen extends Screen {
    public static final Identifier GUI_TEXTURE =
            Identifier.of(HackerMod.MOD_ID, "textures/gui/hacker_hand/hacker_hand_gui_block.png");

    private final BlockPos blockPos;
    private final BlockState blockState;

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

        // Close button for now (we'll add real functionality later)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(centerX - 50, centerY + 50, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Display block info
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Block: " + blockState.getBlock().getName().getString()),
                this.width / 2,
                40,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Position: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()),
                this.width / 2,
                60,
                0xAAAAAA
        );
    }

    @Override
    public boolean shouldPause() {
        return true; // Pauses the game when open
    }
}