package com.flock.atgb.service;

import com.flock.atgb.google.MapRoute;
import com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.dto.TrafficReminderDto;
import com.flock.atgb.exception.FlockException;
import com.flock.atgb.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * Created by B0095829 on 4/2/17.
 */
public class ReminderTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ReminderTask.class.getCanonicalName());

    private SlashEvent slashEvent;
    private TrafficReminderDto reminderDto;
    private int count = 0;
    private boolean useCoordinates;

    public ReminderTask(SlashEvent slashEvent, TrafficReminderDto reminderDto, boolean useCoordinates) {
        this.slashEvent = slashEvent;
        this.reminderDto = reminderDto;
        this.useCoordinates = useCoordinates;
    }


    public ReminderTask(SlashEvent slashEvent, TrafficReminderDto reminderDto, boolean useCoordinates, int count) {
        this.slashEvent = slashEvent;
        this.reminderDto = reminderDto;
        this.useCoordinates = useCoordinates;
        this.count = count;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {


        try {
            long timeTakenSecOriginal = slashEvent.getTimenTakenSec();

            // Find Current Best Route
            MapRouteFinder finder;
            if (useCoordinates) {
                finder = MapRouteFinder.createRouteFinder(slashEvent.getSourceLat(), slashEvent.getSourceLng(), slashEvent.getDestinationLat(), slashEvent.getDestinationLng());
            } else {
                finder = MapRouteFinder.createRouteFinder(reminderDto.getSource(), reminderDto.getDestination());
            }

            MapRoute bestRouteByDuration = finder.getBestRouteByDuration();

            long diff = bestRouteByDuration.getDuration() - timeTakenSecOriginal;

            // currTimeTaken > previous (Route Busy) - 5min window
            if (diff > 5 * 60) {
                // Send Notification with expected reaching time
                CommonUtils.sendNotification(bestRouteByDuration, slashEvent, reminderDto.getFinalDestinationDate());
                Map<String, Object> queryParams = new HashMap<String, Object>();
                queryParams.put("userId", slashEvent.getUserId());
                queryParams.put("alarmTs", slashEvent.getAlarmTs());
                CommonUtils.delete(queryParams);

                // No need to Update DB Status as we are query using TS

            } else {
//                if (count >= 2) {
//                    logger.info("Limit to Set Timer Exhaust for UserId [{}] userName [{}] ", slashEvent.getUserId(), slashEvent.getUserName());

                    // Send Notification with expected reaching time
                    CommonUtils.sendNotification(bestRouteByDuration, slashEvent, reminderDto.getFinalDestinationDate());
                    Map<String, Object> queryParams = new HashMap<String, Object>();
                    queryParams.put("userId", slashEvent.getUserId());
                    queryParams.put("alarmTs", slashEvent.getAlarmTs());
                    CommonUtils.delete(queryParams);
                    //return;
//                }//
//                this.count++;
                // Shift timer to mid diff
                //TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto, useCoordinates, count), (diff / 2 + 5 * 60) * 1000);
            }
        } catch (FlockException e) {
            e.printStackTrace();
        }


    }
}
