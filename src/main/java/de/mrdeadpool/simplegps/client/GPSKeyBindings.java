package de.mrdeadpool.simplegps.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;


@EventBusSubscriber(
        modid = de.mrdeadpool.simplegps.SimpleGPS.MOD_ID,
        value = Dist.CLIENT
)
public class GPSKeyBindings {

    public static final KeyMapping OPEN_HISTORY = new KeyMapping(
            "GPS Verlauf",          // Name (für Übersetzung)
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,                  // Standard: G
            "Simple GPS"        // Kategorie in den Einstellungen
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HISTORY);
    }
}