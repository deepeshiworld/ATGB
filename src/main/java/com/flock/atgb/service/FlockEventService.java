package com.flock.atgb.service;

import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.FlockUser;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.dto.TrafficReminderDto;
import com.flock.atgb.util.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by B0095829 on 4/1/17.
 */
@Service
public class FlockEventService {

    private static final Logger logger = LoggerFactory.getLogger(FlockEventService.class.getCanonicalName());
    private static Gson gson = new Gson();

    @Autowired
    FlockDbService flockDbService;

    public boolean processAppInstall(String payload) {

        try {
            FlockUser flockUser = gson.fromJson(payload, FlockUser.class);
            flockUser.setActive(true);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("token", flockUser.getToken());
            params.put("isActive", flockUser.isActive());
            return flockDbService.updateUserInDb(flockUser, params, true);
//            return flockDbService.addUserInDb(flockUser);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean processTrafficUpdateRequest(String payload) {

        try {
            SlashEvent slashEvent = new SlashEvent();
            slashEvent = slashEvent.fromJson(payload);
            slashEvent.setTaskId(System.currentTimeMillis());

            String slashEventText = slashEvent.getText();
            if (StringUtils.isNotBlank(slashEventText)) {

                TrafficReminderDto reminderDto = new TrafficReminderDto();
                reminderDto.parse(slashEventText);

                // If arrivalDate has been passed
                if (reminderDto.getFinalDestinationDate().before(new Date())) {
                    logger.error("Time has passed , cannot set alarm");
                    return false;
                }
                MapRouteFinder finder = MapRouteFinder.createRouteFinder(reminderDto.getSource(), reminderDto.getDestination());

                MapRoute bestRouteByDuration = finder.getBestRouteByDuration();
                slashEvent.setTimenTakenSec(bestRouteByDuration.getDuration());
                slashEvent.setActive(true);

                // Save to DB
                flockDbService.addTrafficDataInDB(slashEvent);

                // Set ReminderTask
                return setTimer(bestRouteByDuration, slashEvent, reminderDto);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    private boolean setTimer(MapRoute bestRouteByDuration, SlashEvent slashEvent, TrafficReminderDto reminderDto) {

        Long bestRouteByDurationSec = bestRouteByDuration.getDuration();
        // e.g. 4pm
        DateTime finalDestinationTime = new DateTime(reminderDto.getFinalDestinationDate());

        // e.g. 3pm
        DateTime departureTime = new DateTime(finalDestinationTime.minusSeconds(bestRouteByDurationSec.intValue()));

        DateTime nowDate = new DateTime();

        if (departureTime.getMillis() < nowDate.getMillis()) {
            logger.info("Traffic Update Cannot be set for the user");
            CommonUtils.sendNotification(bestRouteByDuration, slashEvent, reminderDto);
            return true;
        }
        // Add 1
        TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), departureTime.toDate());

        // Add 2
        //TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), departureTime.minusMinutes(10).toDate());

        // Add 3
        //TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), departureTime.plusMinutes(10).toDate());

        return true;
    }

    public boolean processAppUninstall(JSONObject payload) {
        try {
            String userId = (String) payload.get("userId");
            FlockUser flockUser = flockDbService.getUserFromUserId(userId);
            if (flockUser == null || StringUtils.isBlank(flockUser.getUserId())) {
                return true;
            }
            flockUser.setActive(false);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userId", flockUser.getUserId());
            params.put("isActive", flockUser.isActive());
            return flockDbService.updateUserInDb(flockUser, params, false);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void handlePressEvent(String payload) {
    }
}
