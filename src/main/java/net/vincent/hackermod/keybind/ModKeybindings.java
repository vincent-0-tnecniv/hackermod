package net.vincent.hackermod.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.vincent.hackermod.HackerMod;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {
    public static KeyBinding hackerMenuKey;

    public static void registerKeys() {
        hackerMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hackermod.hacker_menu", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,             // Default key: V
                "category.hackermod"         // Category
        ));

        HackerMod.LOGGER.info("Registering Key Binds for " + HackerMod.MOD_ID);
    }
}