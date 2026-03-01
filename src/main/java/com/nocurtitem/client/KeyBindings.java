package com.nocurtitem.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBindings {

    public static final String CATEGORY = "key.nocurtitem.category";
    public static final String OPEN_MENU = "key.nocurtitem.open_menu";

    private static KeyBinding openMenuKey;

    public static void register() {
        openMenuKey = new KeyBinding(
                OPEN_MENU,
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_CONTROL,
                CATEGORY
        );
        ClientRegistry.registerKeyBinding(openMenuKey);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openMenuKey != null && openMenuKey.consumeClick()) {
            ClientSetup.openNoPickupScreen();
        }
    }
}
