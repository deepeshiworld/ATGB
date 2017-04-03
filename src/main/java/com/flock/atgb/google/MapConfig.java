package com.flock.atgb.google;

import com.google.maps.GeoApiContext;

/**
 * Created by amber on 01-04-2017.
 */
public class MapConfig {
    private static final String GOOGLE_API_KEY = "AIzaSyC-0d-QNF-Hwx-qnMkE_lg7G5vWzcWwm3A";

    static final GeoApiContext mapContext = new GeoApiContext().setApiKey(GOOGLE_API_KEY);
}
