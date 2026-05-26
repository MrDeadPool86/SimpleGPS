package de.mrdeadpool.simplegps.client;

public record GPSPortalLink(
        String name,

        String fromDimension,
        double fromX,
        double fromY,
        double fromZ,

        String toDimension,
        double toX,
        double toY,
        double toZ
) {}