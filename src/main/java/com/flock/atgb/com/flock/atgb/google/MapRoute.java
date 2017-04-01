package com.flock.atgb.com.flock.atgb.google;

import com.google.maps.model.Distance;

/**
 * Created by amber on 01-04-2017.
 */
public class MapRoute {
    private long distanceInMeters;
    private long durationInSeconds;
    private String[] warnings;

    private MapRoute(final long distance, final long duration, final String[] warnings) {
        this.distanceInMeters = distance;
        this.durationInSeconds = duration;
        this.warnings = warnings;
    }

    public static enum DISTANCE_UNIT {
        KILLOMETERS,
        METERS,
        MILES
    }

    public double getDistance(final DISTANCE_UNIT unit) {
        double distance = 0;
        switch (unit) {
            case KILLOMETERS:
                distance = distanceInMeters / 1000.0;
                break;
            case METERS:
                distance = distanceInMeters;
                break;
            case MILES:
                distance = distanceInMeters * 0.000621371;
                break;
        }
        return distance;
    }

    public long getDuration() {
        return durationInSeconds;
    }

    static MapRoute createRoute(final long distance, final long duration, final String[] warnings) {
        return new MapRoute(distance, duration, warnings);
    }

}
