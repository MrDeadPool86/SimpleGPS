package de.mrdeadpool.simplegps.client;

import de.mrdeadpool.simplegps.GPSSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.sounds.SoundEvent;

@EventBusSubscriber(modid = de.mrdeadpool.simplegps.SimpleGPS.MOD_ID,
        value = Dist.CLIENT)
public class GPSHudRenderer {

    // Textur-Pfad definieren (einmal als Konstante)
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("simplegps", "textures/gui/arrow.png");

    private static final double ARRIVAL_SQ = 1.0;  // 1 Block Radius

    @SubscribeEvent
    public static void onRender(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        GPSSettings.tick();
        if (!GPSClientData.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        GPSPortalNavigator.updatePortalTargetIfNeeded();

        // ── Prüfen ob Zwischenziel aktiv war und Spieler jetzt in der Zieldimension ist ──
        String currentDim = player.level().dimension().location().toString();

        if (GPSClientData.isPortalTarget()
                && GPSClientData.isWaitingForDimensionChange()
                && currentDim.equals(GPSClientData.getFinalDimension())) {

            GPSClientData.restoreFinalTarget();

            player.displayClientMessage(
                    Component.literal("§a[GPS] §f")
                            .append(Component.translatable("gps.nav.dim.reached")),
                    true
            );
        }

        double dx = GPSClientData.getX() - player.getX();
        double dy = GPSClientData.getY() - player.getY();
        double dz = GPSClientData.getZ() - player.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        // Ankunft erkannt → aufräumen
        if (distSq <= ARRIVAL_SQ) {
            if (GPSRouteManager.isActive()) {
                // Route läuft – nächsten Wegpunkt
                String wpName = GPSClientData.getName();
                boolean routeDone = GPSRouteManager.onWaypointReached();

                if (mc.player != null) {
                    if (routeDone) {
                        // Route beendet
                        mc.player.displayClientMessage(
                                Component.literal("§a[GPS] §f")
                                        .append(Component.translatable("gps.nav.route.end")),
                                true
                        );
                        mc.player.playSound(
                                GPSSounds.ARRIVAL.get(),
                                GPSSettings.getSoundVolume(),
                                1.0f
                        );
                        de.mrdeadpool.simplegps.network.GPSClientNetwork.sendDelete();
                    } else {
                        // Wegpunkt erreicht, weiter zur nächsten
                        mc.player.displayClientMessage(
                                Component.literal("§e[GPS] §f")
                                        .append(Component.translatable("gps.nav.reached")),
                                true
                        );
                        mc.player.playSound(
                                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                                GPSSettings.getSoundVolume(),
                                1.0f
                        );
                        // Nächsten Wegpunkt als Paket senden
                        GPSHistoryEntry next = GPSRouteManager.getCurrentWaypoint();
                        if (next != null) {
                            de.mrdeadpool.simplegps.network.GPSClientNetwork
                                    .sendSetWaypoint(next.name(), next.x(), next.y(), next.z(), "white", next.dimension());
                        }
                    }
                }
            } else {
                // Normale Navigation
                String zielName = GPSClientData.getName();

                if (GPSClientData.isPortalTarget()) {

                    if (!GPSClientData.isWaitingForDimensionChange()) {
                        GPSClientData.setWaitingForDimensionChange(true);

                        if (mc.player != null) {
                            mc.player.displayClientMessage(
                                    Component.literal("§e[GPS] §f")
                                            .append(Component.translatable("gps.nav.porta.reached")),
                                    true
                            );
                            mc.player.playSound(
                                    SoundEvents.PLAYER_LEVELUP,
                                    GPSSettings.getSoundVolume(),
                                    1.0f
                            );
                        }
                    }

                    return;
                }

                GPSClientData.clear();
                de.mrdeadpool.simplegps.network.GPSClientNetwork.sendDelete();

                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            Component.literal("§f")
                                    .append(Component.translatable("gps.nav.you"))
                                    .append(" §b'" + zielName + "' §f")
                                    .append(Component.translatable("gps.nav.you.reach")),
                            true
                    );
                    String lang = Minecraft.getInstance()
                            .getLanguageManager()
                            .getSelected();

                    SoundEvent soundToPlay =
                            lang.startsWith("de_")
                                    ? GPSSounds.ARRIVAL.get()
                                    : GPSSounds.ARRIVAL_INT.get();

                    mc.player.playSound(
                            soundToPlay,
                            GPSSettings.getSoundVolume(),
                            1.0f
                    );
                }
            }
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() - 62;

        // Pfeilwinkel berechnen
        double targetAngle = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;

        double relAngle = targetAngle - player.getYRot();

// normalisieren
        while (relAngle < -180) relAngle += 360;
        while (relAngle > 180) relAngle -= 360;

        double drawAngle = relAngle;

        drawArrow(g, cx, cy, drawAngle, false);

        int dist = (int)Math.sqrt(distSq);

        // Farbe je nach Distanz
        String distColor;
        if (dist <= 50) {
            distColor = "§a"; // grün
        } else if (dist <= 200) {
            distColor = "§e"; // gelb
        } else {
            distColor = "§c"; // rot
        }// Name + Distanz

        String label = "§b" + GPSClientData.getName()
                + " " + distColor + "(" + dist + "m)";
        Component txt = Component.literal(label);
        g.drawString(mc.font, txt,
                cx - mc.font.width(txt) / 2,
                cy + 14, 0xFFFFFFFF, true);


    }

    private static void drawArrow(GuiGraphics g, int cx, int cy,
                                  double deg, boolean offScreen) {
        int size = 24;
        float alpha = offScreen ? 0.5f : 1.0f;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) deg));
        pose.translate(-size / 2f, -size / 2f, 0);

        // Farbe setzen VOR dem blit
        switch (GPSClientData.getColor()) {
            case "white"    -> RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            case "green"  -> RenderSystem.setShaderColor(0f, 1f, 0f, alpha);
            case "blue"   -> RenderSystem.setShaderColor(0f, 0.5f, 1f, alpha);
            case "yellow" -> RenderSystem.setShaderColor(1f, 1f, 0f, alpha);
            default       -> RenderSystem.setShaderColor(1f, 0f, 0f, alpha);
        }

        g.blit(ARROW_TEXTURE, 0, 0, 0, 0, size, size, size, size);
        pose.popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}