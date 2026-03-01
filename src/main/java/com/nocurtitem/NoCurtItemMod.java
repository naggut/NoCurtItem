package com.nocurtitem;

import com.nocurtitem.config.BlockedItemsConfig;
import com.nocurtitem.event.ItemPickupHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NoCurtItemMod.MOD_ID)
public class NoCurtItemMod {

    public static final String MOD_ID = "nocurtitem";
    public static final Logger LOGGER = LogManager.getLogger();

    public NoCurtItemMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(new ItemPickupHandler());
        BlockedItemsConfig.load();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        if (FMLEnvironment.dist.isClient()) {
            event.enqueueWork(() -> com.nocurtitem.client.ClientSetup.init());
        }
    }
}
