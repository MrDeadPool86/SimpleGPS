package de.mrdeadpool.simplegps.client;

public record GPSHistoryEntry(
        String name,
        double x,
        double y,
        double z,
        String date,  // Format: "DD.MM HH:mm"
        String dimension
) {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GPSHistoryEntry e)) return false;

        return Double.compare(e.x, x) == 0 &&
                Double.compare(e.y, y) == 0 &&
                Double.compare(e.z, z) == 0 &&
                java.util.Objects.equals(name, e.name) &&
                java.util.Objects.equals(dimension, e.dimension);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, x, y, z, dimension);
    }
}