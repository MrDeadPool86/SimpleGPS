package de.mrdeadpool.simplegps.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = de.mrdeadpool.simplegps.SimpleGPS.MOD_ID,
        value = Dist.CLIENT
)
public class GPSKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Nur wenn Spieler im Spiel ist und kein anderes GUI offen
        if (mc.player == null || mc.screen != null) return;

        while (GPSKeyBindings.OPEN_HISTORY.consumeClick()) {
            mc.setScreen(new GPSHistoryScreen());
        }
    }
}