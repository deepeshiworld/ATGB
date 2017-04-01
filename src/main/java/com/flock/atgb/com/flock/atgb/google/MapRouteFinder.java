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
    private LatLng sourceLocation;
    private LatLng destinationLocation;

    List<MapRoute> mapRoutes = new ArrayList<MapRoute>();

    private volatile boolean hasExceptionOccurred;
    private volatile Exception exception;
    private volatile boolean isThreadCompleted;

    private MapRouteFinder(final LatLng source, final LatLng destination) {
        this.sourceLocation = source;
        this.destinationLocation = destination;

        new Thread(this).start();
    }

    public static MapRouteFinder createRouteFinder(final double sourceLat, final double sourceLang, final double destinationLat, final double destinationLang) {
        return new MapRouteFinder(new LatLng(sourceLat, sourceLang), new LatLng(destinationLat, destinationLang));
    }

    public MapRoute getBestRouteByDistance() throws FlockException {
        if (hasExceptionOccurred) {
            throw new FlockException("failed to find route information", exception);
        }

        synchronized (this) {
            if (!isThreadCompleted) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        if (hasExceptionOccurred) {
            throw new FlockException("failed to find route information", exception);
        }

        synchronized (this) {
            if (!isThreadCompleted) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        if (hasExceptionOccurred) {
            throw new FlockException("failed to find route information", exception);
        }

        synchronized (this) {
            if (!isThreadCompleted) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return mapRoutes;
    }

    @Override
    public void run() {
        GeocodingResult[] sources = null;
        GeocodingResult[] destinations = null;
        try {
            sources = GeocodingApi.reverseGeocode(MapConfig.mapContext, sourceLocation).await();
            if (sources == null) {
                throw new FlockException("no location present for given source coordinates");
            }
            if (sources.length > 1) {
                throw new FlockException("multiple location present for given source coordinates");
            }

            destinations = GeocodingApi.reverseGeocode(MapConfig.mapContext, destinationLocation).await();
            if (destinations == null) {
                throw new FlockException("no location present for given destination coordinates");
            }
            if (destinations.length > 1) {
                throw new FlockException("multiple location present for given destination coordinates");
            }

            DirectionsResult directionResults = DirectionsApi.getDirections(MapConfig.mapContext, sources[0].formattedAddress,
                    destinations[0].formattedAddress).await();
            if (directionResults == null || directionResults.routes == null || directionResults.routes.length == 0) {
                throw new FlockException("no directions available between source and destination");
            }

            for(int i = 0 ; i < directionResults.routes.length; i++){
                long duration = 0;
                long distance = 0;
                String[] warnings = null;

                DirectionsRoute route = directionResults.routes[i];
                warnings = route.warnings;
                if(route.legs == null || route.legs.length == 0){
                    continue;
                }

                duration = route.legs[0].duration.inSeconds;
                distance = route.legs[0].distance.inMeters;
                for(int j = 1 ; j < route.legs.length; j++){
                    DirectionsLeg leg = route.legs[j];
                    if(duration > leg.duration.inSeconds){
                        duration = leg.duration.inSeconds;
                        distance = leg.distance.inMeters;
                    }
                }
                this.mapRoutes.add(MapRoute.createRoute(distance, duration, warnings));
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
