package de.mrdeadpool.simplegps.client;

public class GPSClientData {

    private static boolean active = false;
    private static String name = "";

    private static double targetX, targetY, targetZ;
    private static String color = "white";
    private static String dimension = "minecraft:overworld";

    // Portal-System
    private static boolean portalTarget = false;
    private static boolean waitingForDimensionChange = false;

    private static String finalName = "";
    private static double finalX, finalY, finalZ;
    private static String finalDimension = "minecraft:overworld";

    // ───────── NORMAL ─────────
    public static void set(String n, double x, double y, double z, String c, String dim) {
        active = true;
        name = n;

        targetX = x;
        targetY = y;
        targetZ = z;

        color = c;
        dimension = dim;

        portalTarget = false;
        waitingForDimensionChange = false;

        // echtes Ziel = aktuelles Ziel
        finalName = n;
        finalX = x;
        finalY = y;
        finalZ = z;
        finalDimension = dim;
    }

    // ───────── PORTAL ─────────
    public static void setPortalTarget(
            String portalName,
            double px, double py, double pz,
            String c,
            String finalNameIn,
            double fx, double fy, double fz,
            String finalDim
    ) {
        active = true;
        name = portalName;

        targetX = px;
        targetY = py;
        targetZ = pz;

        color = c;
        dimension = finalDim;

        portalTarget = true;
        waitingForDimensionChange = false;

        finalName = finalNameIn;
        finalX = fx;
        finalY = fy;
        finalZ = fz;
        finalDimension = finalDim;
    }

    public static void restoreFinalTarget() {
        if (finalName == null || finalDimension == null) return;

        active = true;
        name = finalName;

        targetX = finalX;
        targetY = finalY;
        targetZ = finalZ;

        color = "white";
        dimension = finalDimension;

        portalTarget = false;
        waitingForDimensionChange = false;
    }

    // ───────── GETTER ─────────
    public static boolean isActive() { return active; }
    public static String getName() { return name; }

    public static double getX() { return targetX; }
    public static double getY() { return targetY; }
    public static double getZ() { return targetZ; }

    public static String getColor() { return color; }
    public static String getDimension() { return dimension; }

    public static boolean isPortalTarget() { return portalTarget; }

    public static String getFinalName() { return finalName; }
    public static double getFinalX() { return finalX; }
    public static double getFinalY() { return finalY; }
    public static double getFinalZ() { return finalZ; }
    public static String getFinalDimension() { return finalDimension; }
    public static boolean isWaitingForDimensionChange() {
        return waitingForDimensionChange;
    }

    public static void setWaitingForDimensionChange(boolean waiting) {
        waitingForDimensionChange = waiting;
    }

    public static void clear() {
        active = false;
        name = "";
        portalTarget = false;
        waitingForDimensionChange = false;

        finalName = "";
        finalX = 0;
        finalY = 0;
        finalZ = 0;
        finalDimension = "minecraft:overworld";
    }
}