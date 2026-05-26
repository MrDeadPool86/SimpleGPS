package de.mrdeadpool.simplegps.client;

import net.minecraft.client.Minecraft;

import java.util.List;

public class GPSPortalNavigator {

    public static void updatePortalTargetIfNeeded() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!GPSClientData.isActive()) return;

        String currentDim = mc.level.dimension().location().toString();
        String targetDim = GPSClientData.getFinalDimension();

        if (targetDim == null || targetDim.isBlank()) return;

        // Wenn wir schon in der Zieldimension sind und noch ein Portal-Zwischenziel aktiv ist:
        if (GPSClientData.isPortalTarget() && currentDim.equals(targetDim)) {
            GPSClientData.restoreFinalTarget();
            return;
        }

        // Wenn wir NICHT in Portalmodus sind und Ziel in anderer Dimension liegt:
        if (!GPSClientData.isPortalTarget() && !currentDim.equals(targetDim)) {
            List<GPSPortalLink> portals = GPSPortals.getAll();

            for (GPSPortalLink portal : portals) {
                // Von A -> B
                if (portal.fromDimension().equals(currentDim)
                        && portal.toDimension().equals(targetDim)) {

                    GPSClientData.setPortalTarget(
                            "Portal: " + portal.name(),
                            portal.fromX(),
                            portal.fromY(),
                            portal.fromZ(),
                            "gelb",
                            GPSClientData.getName(),
                            GPSClientData.getX(),
                            GPSClientData.getY(),
                            GPSClientData.getZ(),
                            targetDim
                    );
                    return;
                }

                // Von B -> A
                if (portal.toDimension().equals(currentDim)
                        && portal.fromDimension().equals(targetDim)) {

                    GPSClientData.setPortalTarget(
                            "Portal: " + portal.name(),
                            portal.toX(),
                            portal.toY(),
                            portal.toZ(),
                            "gelb",
                            GPSClientData.getName(),
                            GPSClientData.getX(),
                            GPSClientData.getY(),
                            GPSClientData.getZ(),
                            targetDim
                    );
                    return;
                }
            }
        }
    }
}