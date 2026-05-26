package de.mrdeadpool.simplegps.client;

import java.util.ArrayList;
import java.util.List;

public class GPSCategory {
    private String name;
    private final List<GPSHistoryEntry> entries = new ArrayList<>();

    public GPSCategory(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<GPSHistoryEntry> getEntries() { return entries; }

    public void addEntry(GPSHistoryEntry entry) {
        boolean exists = entries.stream().anyMatch(e ->
                e.name().equals(entry.name()) &&
                        e.x() == entry.x() &&
                        e.y() == entry.y() &&
                        e.z() == entry.z() &&
                        e.dimension().equals(entry.dimension())
        );
        if (!exists) entries.add(entry);
    }

    public void removeEntry(GPSHistoryEntry entry) {
        entries.remove(entry);
    }
}