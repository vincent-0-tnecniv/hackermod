package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.networking.CommandPacket;

import java.util.ArrayList;
import java.util.List;

public class HackerHandCommandScreen extends Screen {
    private TextFieldWidget commandField;
    private final List<String> commandHistory = new ArrayList<>();
    private int selectedCommandIndex = -1;

    public HackerHandCommandScreen() {
        super(Text.literal("Command Menu"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Title
        TextWidget titleLabel = new TextWidget(
                centerX - 150,
                centerY - 80,
                300,
                20,
                Text.literal("H4CK3R's Command Console"),
                this.textRenderer
        );
        titleLabel.setTextColor(0x00FF00);

        // Command label
        TextWidget commandLabel = new TextWidget(
                centerX - 150,
                centerY - 45,
                80,
                20,
                Text.literal("Command:"),
                this.textRenderer
        );
        commandLabel.setTextColor(0x00AAFF);

        // Command input field
        this.commandField = new TextFieldWidget(
                this.textRenderer,
                centerX - 60,
                centerY - 45,
                210,
                20,
                Text.literal("Enter command")
        );

        // Execute button
        ButtonWidget executeButton = ButtonWidget.builder(
                Text.literal("Execute"),
                button -> executeCommand()
        ).dimensions(centerX - 80, centerY + 30, 70, 20).build();

        // Clear button
        ButtonWidget clearButton = ButtonWidget.builder(
                Text.literal("Clear"),
                button -> {
                    commandField.setText("");
                }
        ).dimensions(centerX, centerY + 30, 70, 20).build();

        // Close button
        ButtonWidget closeButton = ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(centerX + 80, centerY + 30, 70, 20).build();

        // Add all widgets
        this.addDrawableChild(titleLabel);
        this.addDrawableChild(commandLabel);
        this.addDrawableChild(this.commandField);
        this.addDrawableChild(executeButton);
        this.addDrawableChild(clearButton);
        this.addDrawableChild(closeButton);
    }

    private void executeCommand() {
        String command = commandField.getText();

        if (command.isEmpty()) {
            return;
        }

        // Add to history
        commandHistory.add(0, command);
        if (commandHistory.size() > 10) {
            commandHistory.remove(commandHistory.size() - 1);
        }
        String fullCommand = command;

        // Send as player command (without the / if present)
        String commandToSend = fullCommand.startsWith("/") ?
                fullCommand.substring(1) : fullCommand;

        // Execute command
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendCommand(commandToSend);
            HackerMod.LOGGER.info("Executing command: {}", commandToSend);
        }

        // Optional: Show confirmation
        MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§aExecuted: §f" + fullCommand),
                true
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle command history navigation (up/down arrows)
        if (keyCode == 265) { // Up arrow
            if (selectedCommandIndex < commandHistory.size() - 1) {
                selectedCommandIndex++;
                commandField.setText(commandHistory.get(selectedCommandIndex));
            }
            return true;
        } else if (keyCode == 264) { // Down arrow
            if (selectedCommandIndex > 0) {
                selectedCommandIndex--;
                commandField.setText(commandHistory.get(selectedCommandIndex));
            } else if (selectedCommandIndex == 0) {
                selectedCommandIndex = -1;
                commandField.setText("");
            }
            return true;
        }

        // Let text fields handle their own key presses
        if (this.commandField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCustomBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xB00A0A2A);

        // Draw a "terminal" line effect
        for (int i = 0; i < this.height; i += 20) {
            context.fill(0, i, this.width, i + 1, 0x1100FF66);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Prevent default background
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}