package de.mrdeadpool.simplegps;

import de.mrdeadpool.simplegps.network.GPSNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(SimpleGPS.MOD_ID)
public class SimpleGPS {
    public static final String MOD_ID = "simplegps";

    public SimpleGPS(IEventBus modEventBus) {
        GPSNetwork.register(modEventBus);
        GPSSounds.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Client-Event für Historie-Laden
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) -> {
            de.mrdeadpool.simplegps.client.GPSHistory.load();
            de.mrdeadpool.simplegps.client.GPSFavorites.load();
            de.mrdeadpool.simplegps.client.GPSCategories.load();
            de.mrdeadpool.simplegps.client.GPSRoutes.load();
            de.mrdeadpool.simplegps.client.GPSPortals.load();
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        de.mrdeadpool.simplegps.command.GPSCommand.register(event.getDispatcher());
    }
}