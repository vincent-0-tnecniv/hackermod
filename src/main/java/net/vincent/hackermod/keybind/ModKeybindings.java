package net.vincent.hackermod.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.vincent.hackermod.HackerMod;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {
    public static KeyBinding commandMenuKey;
    public static KeyBinding summonMenuKey;
    public static KeyBinding flyToggleKey;

    public static void registerKeys() {
        commandMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hackermod.command_menu", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,             // Default key: V
                "category.hackermod"         // Category
        ));

        summonMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hackermod.summon_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.hackermod"
        ));

        flyToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hackermod.fly_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.hackermod"
        ));

        HackerMod.LOGGER.info("Registering Key Binds for " + HackerMod.MOD_ID);
    }
}