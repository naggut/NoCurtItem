package com.nocurtitem.client;

import com.nocurtitem.NoCurtItemMod;
import net.minecraft.client.Minecraft;

public class ClientSetup {

    public static void init() {
        KeyBindings.register();
        NoCurtItemMod.LOGGER.debug("NoCurtItem client setup done");
    }

    public static void openNoPickupScreen() {
        Minecraft.getInstance().setScreen(new NoPickupScreen());
    }
}
