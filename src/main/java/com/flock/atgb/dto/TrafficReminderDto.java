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

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
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
}
