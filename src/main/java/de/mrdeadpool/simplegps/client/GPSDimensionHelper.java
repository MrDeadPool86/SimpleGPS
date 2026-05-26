package de.mrdeadpool.simplegps.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class GPSDimensionHelper {

    public static List<DimensionOption> getAvailableDimensions() {
        Minecraft mc = Minecraft.getInstance();
        List<DimensionOption> result = new ArrayList<>();

        // Bester Weg: alle bekannten Dimensionen aus der aktuellen Verbindung holen
        if (mc.getConnection() != null) {
            Set<ResourceKey<Level>> levels = mc.getConnection().levels();

            for (ResourceKey<Level> key : levels) {
                String id = key.location().toString();

                // DEBUG
                System.out.println("[SimpleGPS] Dimension gefunden: " + id);

                result.add(new DimensionOption(id, toDisplayName(id)));
            }
        }

        // Fallback
        if (result.isEmpty()) {
            addVanillaFallback(result);
        }

        result.sort(Comparator.comparing(DimensionOption::displayName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private static void addVanillaFallback(List<DimensionOption> result) {
        result.add(new DimensionOption("minecraft:overworld", "Overworld"));
        result.add(new DimensionOption("minecraft:the_nether", "Nether"));
        result.add(new DimensionOption("minecraft:the_end", "End"));
    }

    public static String toDisplayName(String id) {
        return switch (id) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "End";
            default -> prettify(id);
        };
    }

    private static String prettify(String id) {
        String path = id.contains(":") ? id.split(":", 2)[1] : id;
        path = path.replace('/', ' ');
        path = path.replace('_', ' ');
        path = path.replace('-', ' ');

        String[] parts = path.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }

        return sb.toString().trim();
    }
}