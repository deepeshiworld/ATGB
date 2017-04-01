package com.flock.atgb.service;

import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.dto.TrafficReminderDto;
import com.flock.atgb.exception.FlockException;
import com.flock.atgb.util.CommonUtils;

import java.util.TimerTask;

/**
 * Created by B0095829 on 4/2/17.
 */
public class ReminderTask extends TimerTask {

    private SlashEvent slashEvent;
    private TrafficReminderDto reminderDto;
    private int count = 0;

    public ReminderTask(SlashEvent slashEvent, TrafficReminderDto reminderDto) {
        this.slashEvent = slashEvent;
        this.reminderDto = reminderDto;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {


        try {
            long timenTakenSecOriginal = slashEvent.getTimenTakenSec();

            // Find Current Best Route
            MapRouteFinder finder = MapRouteFinder.createRouteFinder(reminderDto.getSource(), reminderDto.getDestination());
            MapRoute bestRouteByDuration = finder.getBestRouteByDuration();

            long diff = bestRouteByDuration.getDuration() - timenTakenSecOriginal;

            // currTimeTaking > previous (Route Busy) - 5min window
            if (diff > 5 * 60) {
                // Send Notification with expected reaching time
                CommonUtils.sendNotification(bestRouteByDuration, slashEvent,reminderDto);

                // Update Db Status to inactive
            } else {
                // Shift timer to mid diff
                TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), diff / 2 + 5 * 60);
            }
        } catch (FlockException e) {
            e.printStackTrace();
        }


    }
}
