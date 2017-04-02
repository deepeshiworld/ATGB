package com.flock.atgb.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by B0095829 on 4/2/17.
 */
public class TrafficReminderDto {

    private static String pattern = "yyyy-MM-dd HH:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    private String source;
    private String destination;

    private double sourceLat;
    private double sourceLng;
    private double destinationLat;
    private double destinationLng;


    private Date arrivalDate;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getFinalDestinationDate() {
        return arrivalDate;
    }

    public void setFinalDestinationDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public double getSourceLat() {
        return sourceLat;
    }

    public void setSourceLat(double sourceLat) {
        this.sourceLat = sourceLat;
    }

    public double getSourceLng() {
        return sourceLng;
    }

    public void setSourceLng(double sourceLng) {
        this.sourceLng = sourceLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public void parse(String data) {

        try {
            // Retreive Src & Dest

            String[] splitArr = data.split(",");
            this.source = splitArr[0];
            this.destination = splitArr[1];

            String arrivalDateString = splitArr[2];
            this.arrivalDate = simpleDateFormat.parse(arrivalDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void fill(SlashEvent slashEvent){
        this.setSourceLat(slashEvent.getSourceLat());
        this.setSourceLng(slashEvent.getSourceLng());
        this.setDestinationLat(slashEvent.getSourceLat());
        this.setDestinationLng(slashEvent.getDestinationLng());
    }
}
