package com.flock.atgb.com.flock.atgb.google;

import com.google.maps.model.Distance;

/**
 * Created by amber on 01-04-2017.
 */
public class MapRoute {
    private long distanceInMeters;
    private long durationInSeconds;

    public String getDurationInWords() {
        return durationInWords;
    }

    private String durationInWords;


    public String getSourceName() {
        return sourceName;
    }

    private String sourceName;

    public String getSourceID() {
        return sourceID;
    }

    private String sourceID;

    public String getDestinationID() {
        return destinationID;
    }

    private String destinationID;

    public String getDestinationName() {
        return destinationName;
    }

    private String destinationName;
    private double sourceLat;

    public double getSourceLng() {
        return sourceLng;
    }

    private double sourceLng;

    public double getDestinationLat() {
        return destinationLat;
    }

    private double destinationLat;

    public double getDestinationLng() {
        return destinationLng;
    }

    private double destinationLng;

    public String[] getWarnings() {
        return warnings;
    }

    private String[] warnings;

    private MapRoute() {
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

    public double getSourceLat() {
        return this.sourceLat;
    }


    static MapRoute create() {
        return new MapRoute();
    }

    MapRoute distance(final long distance) {
        this.distanceInMeters = distance;
        return this;
    }

    MapRoute duration(final long duration) {
        this.durationInSeconds = duration;
        return this;
    }

    MapRoute warnings(final String[] warnings) {
        this.warnings = warnings;
        return this;
    }

    MapRoute sourceLat(final double sourceLat) {
        this.sourceLat = sourceLat;
        return this;
    }

    MapRoute sourceLng(final double sourceLng) {
        this.sourceLng = sourceLng;
        return this;
    }

    MapRoute destinationLat(final double destinationLat) {
        this.destinationLat = destinationLat;
        return this;
    }

    MapRoute destinationLng(final double destinationLng) {
        this.destinationLng = destinationLng;
        return this;
    }

    MapRoute sourceName(final String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    MapRoute destinationName(final String destinationName) {
        this.destinationName = destinationName;
        return this;
    }

    MapRoute sourceID(final String sourceID) {
        this.sourceID = sourceID;
        return this;
    }

    MapRoute destinationID(final String destinationID) {
        this.destinationID = destinationID;
        return this;
    }

    MapRoute durationInWords(final String durationInWords) {
        this.durationInWords = durationInWords;
        return this;
    }
}
