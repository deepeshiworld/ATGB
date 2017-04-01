package com.flock.atgb.service;

import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.FlockUser;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.dto.TrafficReminderDto;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by B0095829 on 4/1/17.
 */
@Service
public class FlockEventService {

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

    public void processTrafficUpdateRequest(String payload) {

        try {
            SlashEvent slashEvent = new SlashEvent();
            slashEvent.fromJson(payload);
            slashEvent.setTaskId(System.currentTimeMillis());

            String slashEventText = slashEvent.getText();
            if (StringUtils.isNotBlank(slashEventText)) {

                TrafficReminderDto reminderDto = new TrafficReminderDto();
                reminderDto.parse(slashEventText);

                MapRouteFinder finder = MapRouteFinder.createRouteFinder(reminderDto.getSource(), reminderDto.getDestination());

                MapRoute bestRouteByDuration = finder.getBestRouteByDuration();
                slashEvent.setTimenTakenSec(bestRouteByDuration.getDuration());
                slashEvent.setActive(true);

                // Save to DB
                flockDbService.addTrafficDataInDB(slashEvent);

                // Set ReminderTask
                setTimer(bestRouteByDuration.getDuration(), slashEvent, reminderDto);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setTimer(Long bestRouteByDurationSec, SlashEvent slashEvent, TrafficReminderDto reminderDto) {

        // e.g. 4pm
        DateTime arrivalTime = new DateTime(reminderDto.getArrivalDate());

        // e.g. 3pm
        DateTime departureTime = new DateTime(arrivalTime.minusSeconds(bestRouteByDurationSec.intValue()));

        // Add 1
        TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), departureTime.toDate());

        // Add 2
        //TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), departureTime.minusMinutes(10).toDate());

        // Add 3
        //TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto), departureTime.plusMinutes(10).toDate());

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
