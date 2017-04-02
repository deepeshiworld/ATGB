package com.flock.atgb.service;

import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.FlockUser;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.dto.TrafficReminderDto;
import com.flock.atgb.util.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

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

    public boolean processTrafficUpdateRequest(String payload, boolean useCoordinates) {
        MapRouteFinder finder;
        TrafficReminderDto reminderDto = new TrafficReminderDto();
        try {
            SlashEvent slashEvent = new SlashEvent();
            slashEvent = slashEvent.fromJson(payload);


//            if (StringUtils.isNotBlank(slashEventText)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
            Date d = format.parse(slashEvent.getFinalTimeToReach());

            reminderDto.setFinalDestinationDate(d);

            //TODO
            //slashEvent.setAlarmTs(reminderDto.getFinalDestinationDate().getTime());

            // If arrivalDate has been passed
            if (reminderDto.getFinalDestinationDate().before(new Date())) {
                logger.error("Time has passed , cannot set alarm");
                return false;
            }

            if (useCoordinates) {
                finder = MapRouteFinder.createRouteFinder(slashEvent.getSourceLat(), slashEvent.getSourceLng(), slashEvent.getDestinationLat(), slashEvent.getDestinationLng());
                reminderDto.fill(slashEvent);
            } else {
                String slashEventText = slashEvent.getText();
                reminderDto.parse(slashEventText);
                finder = MapRouteFinder.createRouteFinder(reminderDto.getSource(), reminderDto.getDestination());
            }
            slashEvent.setAlarmTs(reminderDto.getFinalDestinationDate().getTime());
            slashEvent.setFinalTimeToReach(reminderDto.getFinalDestinationDate().toString());

            MapRoute bestRouteByDuration = finder.getBestRouteByDuration();
            slashEvent.setTimenTakenSec(bestRouteByDuration.getDuration());
            slashEvent.setActive(true);
            slashEvent.setSourceName(bestRouteByDuration.getSourceName());
            slashEvent.setDestinationName(bestRouteByDuration.getDestinationName());


            // Save to DB
            flockDbService.addTrafficDataInDB(slashEvent);

            // Set ReminderTask
            return setTimer(bestRouteByDuration, slashEvent, reminderDto, useCoordinates);
            //}


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    private boolean setTimer(MapRoute bestRouteByDuration, SlashEvent slashEvent, TrafficReminderDto reminderDto, boolean useCoordinates) {

        Long bestRouteByDurationSec = bestRouteByDuration.getDuration();
        // e.g. 4pm
        DateTime finalDestinationTime = new DateTime(reminderDto.getFinalDestinationDate());

        // e.g. 3pm
        DateTime departureTime = new DateTime(finalDestinationTime.minusSeconds(bestRouteByDurationSec.intValue()));

        DateTime nowDate = new DateTime();

        if (departureTime.getMillis() < nowDate.getMillis()) {
            logger.info("Traffic Update Cannot be set for the user");
            CommonUtils.sendNotification(bestRouteByDuration, slashEvent, reminderDto.getFinalDestinationDate());
            Map<String, Object> queryParams = new HashMap<String, Object>();
            queryParams.put("userId", slashEvent.getUserId());
            queryParams.put("alarmTs", slashEvent.getAlarmTs());
            CommonUtils.delete(queryParams);
            return true;
        }
        // Add 1
        TrafficReminder.addTaskToTimer(new ReminderTask(slashEvent, reminderDto, useCoordinates), departureTime.toDate());

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
            params.put("isActive", false);
            return flockDbService.updateUserInDb(flockUser, params, false);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void handlePressEvent(String payload) {
    }

    public List<SlashEvent> getUpcomingTrafficUpdates(String userId) {

        List<SlashEvent> events = new ArrayList<>();

        List<DBObject> slashObjects = flockDbService.getSlashObjectByUserIdTs(userId, System.currentTimeMillis());
        for (DBObject slashDbObj : slashObjects) {
            SlashEvent event = new SlashEvent();
            JSONObject jsonObject = new JSONObject(slashDbObj.toMap());
            event = event.fromJson(jsonObject.toJSONString());
            events.add(event);
        }

        return events;
    }
}
