package de.mrdeadpool.simplegps.client;

import java.util.ArrayList;
import java.util.List;

public class GPSRoute {

    private String name;
    private final List<GPSHistoryEntry> waypoints = new ArrayList<>();

    public GPSRoute(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<GPSHistoryEntry> getWaypoints() { return waypoints; }

    public void addWaypoint(GPSHistoryEntry entry) {
        waypoints.add(entry);
    }

    public void removeWaypoint(GPSHistoryEntry entry) {
        waypoints.remove(entry);
    }

    public void removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
        }
    }

    public int size() { return waypoints.size(); }
    public boolean isEmpty() { return waypoints.isEmpty(); }
}