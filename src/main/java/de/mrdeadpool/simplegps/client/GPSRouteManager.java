package de.mrdeadpool.simplegps.client;

public class GPSRouteManager {

    private static GPSRoute activeRoute = null;
    private static int currentWaypointIndex = 0;

    public static void startRoute(GPSRoute route) {
        if (route == null || route.isEmpty()) return;

        activeRoute = route;
        currentWaypointIndex = 0;

        navigateToCurrentWaypoint();
    }

    public static void stopRoute() {
        activeRoute = null;
        currentWaypointIndex = 0;
        GPSClientData.clear();
    }

    public static boolean isActive() {
        return activeRoute != null;
    }

    public static GPSRoute getActiveRoute() {
        return activeRoute;
    }

    public static int getCurrentIndex() {
        return currentWaypointIndex;
    }

    public static GPSHistoryEntry getCurrentWaypoint() {
        if (activeRoute == null) return null;
        if (currentWaypointIndex >= activeRoute.size()) return null;
        return activeRoute.getWaypoints().get(currentWaypointIndex);
    }

    // Wird aufgerufen wenn ein Wegpunkt erreicht wurde
    public static boolean onWaypointReached() {
        if (activeRoute == null) return false;

        currentWaypointIndex++;

        if (currentWaypointIndex >= activeRoute.size()) {
            stopRoute();
            return true;
        }

        navigateToCurrentWaypoint();
        return false;
    }

    private static void navigateToCurrentWaypoint() {
        GPSHistoryEntry wp = getCurrentWaypoint();
        if (wp == null) return;

        GPSClientData.set(
                wp.name() + " §7(" + (currentWaypointIndex + 1) + "/" + activeRoute.size() + ")",
                wp.x(),
                wp.y(),
                wp.z(),
                "white",
                wp.dimension()
        );
    }
}