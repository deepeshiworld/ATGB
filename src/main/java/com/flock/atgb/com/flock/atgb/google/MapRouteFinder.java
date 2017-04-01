package com.flock.atgb.com.flock.atgb.google;

import com.flock.atgb.exception.FlockException;
import com.google.maps.DirectionsApi;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by amber on 01-04-2017.
 */
public class MapRouteFinder implements Runnable {
    List<MapRoute> mapRoutes = new ArrayList<MapRoute>();
    private LatLng sourceLocation;
    private LatLng destinationLocation;
    private String sourceString;
    private String destinationString;
    private int mode = -1;
    
    private volatile boolean hasExceptionOccurred;
    private volatile Exception exception;
    private volatile boolean isThreadCompleted;

    private static final int MODE_LOCATION = 1;
    private static final int MODE_PATH = 2;


    private MapRouteFinder(final LatLng source, final LatLng destination) {
        this.sourceLocation = source;
        this.destinationLocation = destination;
        this.mode = MODE_LOCATION;
        new Thread(this).start();
    }


    private MapRouteFinder(final String source, final String destination) {
        this.sourceString = source;
        this.destinationString = destination;
        this.mode = MODE_PATH;
        new Thread(this).start();
    }

    public static MapRouteFinder createRouteFinder(final double sourceLat, final double sourceLang, final double destinationLat, final double destinationLang) {
        return new MapRouteFinder(new LatLng(sourceLat, sourceLang), new LatLng(destinationLat, destinationLang));
    }

    public static MapRouteFinder createRouteFinder(final String source, final String destination) {
        return new MapRouteFinder(source, destination);
    }

    public MapRoute getBestRouteByDistance() throws FlockException {
        synchronized (this) {
            if (!isThreadCompleted) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (hasExceptionOccurred) {
            throw new FlockException("failed to find route information", exception);
        }

        if (mapRoutes.size() == 0) {
            return null;
        }

        MapRoute route = mapRoutes.get(0);
        Iterator<MapRoute> iterator = mapRoutes.iterator();
        while (iterator.hasNext()) {
            MapRoute value = iterator.next();
            if (value.getDistance(MapRoute.DISTANCE_UNIT.METERS) < route.getDistance(MapRoute.DISTANCE_UNIT.METERS)) {
                route = value;
            }
        }
        return route;
    }

    public MapRoute getBestRouteByDuration() throws FlockException {
        synchronized (this) {
            if (!isThreadCompleted) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (hasExceptionOccurred) {
            throw new FlockException("failed to find route information", exception);
        }

        if (mapRoutes.size() == 0) {
            return null;
        }

        MapRoute route = mapRoutes.get(0);
        Iterator<MapRoute> iterator = mapRoutes.iterator();
        while (iterator.hasNext()) {
            MapRoute value = iterator.next();
            if (value.getDuration() < route.getDuration()) {
                route = value;
            }
        }
        return route;
    }

    public List<MapRoute> getAllRoutes() throws FlockException {
        synchronized (this) {
            if (!isThreadCompleted) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (hasExceptionOccurred) {
            throw new FlockException("failed to find route information", exception);
        }

        return mapRoutes;
    }

    @Override
    public void run() {
        GeocodingResult[] sources = null;
        GeocodingResult[] destinations = null;
        try {

            if (mode == MODE_LOCATION) {
                sources = GeocodingApi.reverseGeocode(MapConfig.mapContext, sourceLocation).await();
                if (sources == null) {
                    throw new FlockException("no location present for given source coordinates");
                }

                destinations = GeocodingApi.reverseGeocode(MapConfig.mapContext, destinationLocation).await();
                if (destinations == null) {
                    throw new FlockException("no location present for given destination coordinates");
                }
            } else if (mode == MODE_PATH) {

                sources = GeocodingApi.geocode(MapConfig.mapContext, sourceString).await();
                if (sources == null) {
                    throw new FlockException("no location present for given source coordinates");
                }

                destinations = GeocodingApi.geocode(MapConfig.mapContext, destinationString).await();
                if (destinations == null) {
                    throw new FlockException("no location present for given destination coordinates");
                }

            } else {
                throw new FlockException("Invalid mode of operation");
            }

            DirectionsResult directionResults = DirectionsApi.getDirections(MapConfig.mapContext, "place_id:" + sources[0].placeId,
                    "place_id:" + destinations[0].placeId).await();
            if (directionResults == null || directionResults.routes == null || directionResults.routes.length == 0) {
                throw new FlockException("no directions available between source and destination");
            }

            for (int i = 0; i < directionResults.routes.length; i++) {
                long duration = 0;
                long distance = 0;
                String[] warnings = null;

                DirectionsRoute route = directionResults.routes[i];
                warnings = route.warnings;
                if (route.legs == null || route.legs.length == 0) {
                    continue;
                }

                duration = route.legs[0].duration.inSeconds;
                distance = route.legs[0].distance.inMeters;
                for (int j = 1; j < route.legs.length; j++) {
                    DirectionsLeg leg = route.legs[j];
                    if (duration > leg.duration.inSeconds) {
                        duration = leg.duration.inSeconds;
                        distance = leg.distance.inMeters;
                    }
                }
                MapRoute mapRoute = MapRoute.create();
                mapRoute.warnings(warnings);
                mapRoute.duration(duration);
                mapRoute.distance(distance);
                mapRoute.sourceLat(sources[0].geometry.location.lat);
                mapRoute.sourceLng(sources[0].geometry.location.lng);
                mapRoute.sourceID(sources[0].placeId);
                mapRoute.sourceName(sources[0].formattedAddress);
                mapRoute.destinationLat(destinations[0].geometry.location.lat);
                mapRoute.destinationLng(destinations[0].geometry.location.lng);
                mapRoute.destinationID(destinations[0].placeId);
                mapRoute.destinationName(destinations[0].formattedAddress);
                this.mapRoutes.add(mapRoute);
            }
        } catch (Exception e) {
            e.printStackTrace();
            hasExceptionOccurred = true;
            exception = e;
        }

        synchronized (this) {
            isThreadCompleted = true;
            this.notifyAll();
        }
    }
}
