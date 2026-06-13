package net.vincent.hackermod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
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

        // WIDER TEXT FIELD - use almost full screen width
        int fieldWidth = this.width - 100;  // Full width minus margins
        int fieldX = 50;                     // Left margin

        // Title
        TextWidget titleLabel = new TextWidget(
                centerX - 200,
                centerY - 80,
                400,
                20,
                Text.literal("H4CK3R's Command Console"),
                this.textRenderer
        );
        titleLabel.setTextColor(0x00FF00);

        // Command label (left aligned)
        TextWidget commandLabel = new TextWidget(
                fieldX,
                centerY - 45,
                80,
                20,
                Text.literal("Command:"),
                this.textRenderer
        );
        commandLabel.setTextColor(0x00AAFF);

        // Command input field - WIDE and with large max length
        this.commandField = new TextFieldWidget(
                this.textRenderer,
                fieldX + 60,               // Start after label
                centerY - 45,
                fieldWidth - 60,           // Full width minus label
                20,
                Text.literal("Enter command")
        );
        this.commandField.setMaxLength(10000);  // Allow very long commands

        // No text restrictions - allow anything
        this.commandField.setTextPredicate(text -> true);

        // Buttons
        int buttonY = centerY + 30;
        int buttonWidth = 100;
        int buttonSpacing = 20;
        int totalButtonWidth = buttonWidth * 3 + buttonSpacing * 2;
        int startX = centerX - totalButtonWidth / 2;

        ButtonWidget executeButton = ButtonWidget.builder(
                Text.literal("Execute"),
                button -> executeCommand()
        ).dimensions(startX, buttonY, buttonWidth, 20).build();

        ButtonWidget clearButton = ButtonWidget.builder(
                Text.literal("Clear"),
                button -> commandField.setText("")
        ).dimensions(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 20).build();

        ButtonWidget closeButton = ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(startX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, 20).build();

        // Add all widgets
        this.addDrawableChild(titleLabel);
        this.addDrawableChild(commandLabel);
        this.addDrawableChild(this.commandField);
        this.addDrawableChild(executeButton);
        this.addDrawableChild(clearButton);
        this.addDrawableChild(closeButton);
    }

    private void executeCommand() {
        String command = commandField.getText().trim();

        if (command.isEmpty()) {
            return;
        }

        // Add to history
        commandHistory.add(0, command);
        if (commandHistory.size() > 20) {
            commandHistory.remove(commandHistory.size() - 1);
        }
        selectedCommandIndex = -1;

        // Send as player command (without the / if present)
        String commandToSend = command.startsWith("/") ? command.substring(1) : command;

        // Execute command
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendCommand(commandToSend);
            HackerMod.LOGGER.info("Executing command: {}", commandToSend);
        }

        // Optional: Keep screen open for multiple commands
        // this.close(); // Don't close - let user run multiple commands
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter key executes command
        if (keyCode == 257 || keyCode == 335) { // Enter or Numpad Enter
            executeCommand();
            return true;
        }

        // Handle command history navigation (up/down arrows)
        if (keyCode == 265) { // Up arrow
            if (selectedCommandIndex < commandHistory.size() - 1) {
                selectedCommandIndex++;
                commandField.setText(commandHistory.get(selectedCommandIndex));
                // Move cursor to end
                commandField.setCursor(commandField.getText().length(), false);
            }
            return true;
        } else if (keyCode == 264) { // Down arrow
            if (selectedCommandIndex > 0) {
                selectedCommandIndex--;
                commandField.setText(commandHistory.get(selectedCommandIndex));
                commandField.setCursor(commandField.getText().length(), false);
            } else if (selectedCommandIndex == 0) {
                selectedCommandIndex = -1;
                commandField.setText("");
            }
            return true;
        }

        // Let text field handle its own key presses
        if (this.commandField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderCustomBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // Show command history size hint
        if (!commandHistory.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("↑/↓ History (" + commandHistory.size() + " commands) | Enter to execute"),
                    centerX,
                    this.height - 25,
                    0x666666
            );
        }
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