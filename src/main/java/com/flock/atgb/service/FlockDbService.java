package com.flock.atgb.service;

import com.flock.atgb.db.MongoDBManager;
import com.flock.atgb.dto.FlockUser;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.util.FlockConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by B0095829 on 4/1/17.
 */
@Service
public class FlockDbService {

    private static final Logger logger = LoggerFactory.getLogger(FlockDbService.class.getCanonicalName());

    @Autowired
    MongoDBManager mongoDBManager;

    public boolean addUserInDb(FlockUser flockUser) {

        if (flockUser == null) {
            return false;
        }

        try {

            if (flockUser != null) {
                mongoDBManager.addObject(FlockConstants.USER, flockUser.toJson());
            }
        } catch (Exception e) {
            logger.error("Error creating FlockUser for : " + flockUser.getUserId() + ". Error : " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updateUserInDb(FlockUser flockUser, Map<String, Object> paramsMap, boolean upsert) {

        if (flockUser == null) {
            return false;
        }

        try {

            if (flockUser != null) {
                Map<String, Object> queryParams = new HashMap<String, Object>();
                queryParams.put("userId", flockUser.getUserId());

                BasicDBObject dbObj = new BasicDBObject();
                dbObj.putAll(paramsMap);

                Map<String, Object> uParams = new HashMap<String, Object>();
                uParams.put("$set", dbObj);

                //mongoDBManager.addObject(FlockConstants.USER, flockUser.toJson());
                mongoDBManager.updateObject(FlockConstants.USER, queryParams, uParams, upsert, false);
            }
        } catch (Exception e) {
            logger.error("Error creating MyAirtelAppUserPreferences for : " + flockUser.getUserId() + ". Error : " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public FlockUser getUserFromUserId(String userId) {

        String userJsonString = null;
        FlockUser flockUser = null;
        try {
            DBObject userObject = getUserObjectByUserId(userId);
            if (userObject != null) {
                userJsonString = userObject.toString();
                flockUser = new FlockUser();
                flockUser.fromJson(userJsonString);
            }
        } catch (Exception e) {
            logger.error("Error fetching User Preferences details for uid :[{}]  ", userId, e, e.getMessage());
        }
        return flockUser;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected DBObject getUserObjectByUserId(String userId) {

        DBObject dbObject = null;
        if (mongoDBManager != null) {
            if (StringUtils.isBlank(userId)) {
                return null;
            }
            Map queryParams = new HashMap<>();
            queryParams.put("userId", userId);
            dbObject = mongoDBManager.getObject(FlockConstants.USER, queryParams);
        }
        return dbObject;
    }

    public boolean addTrafficDataInDB(SlashEvent slashEvent) {
        if (slashEvent == null) {
            return false;
        }

        try {

            if (slashEvent != null) {

                mongoDBManager.addObject(FlockConstants.TRAFFIC_DB, slashEvent.toJson());
            }
        } catch (Exception e) {
            logger.error("Error creating UserPreferences for : " + slashEvent.getUserId() + ". Error : " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updateTrafficDataInDb(SlashEvent SlashEvent, Map<String, Object> paramsMap, boolean upsert) {

        if (SlashEvent == null) {
            return false;
        }

        try {

            if (SlashEvent != null) {
                Map<String, Object> queryParams = new HashMap<String, Object>();
                queryParams.put("userId", SlashEvent.getUserId());
                queryParams.put("alarmTs", SlashEvent.getAlarmTs());

                BasicDBObject dbObj = new BasicDBObject();
                dbObj.putAll(paramsMap);

                Map<String, Object> uParams = new HashMap<String, Object>();
                uParams.put("$set", dbObj);

                mongoDBManager.updateObject(FlockConstants.TRAFFIC_DB, queryParams, uParams, upsert, false);
            }
        } catch (Exception e) {
            logger.error("Error creating MyAirtelAppUserPreferences for : " + SlashEvent.getUserId() + ". Error : " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public SlashEvent getTrafficEventFromTaskId(String taskId, long ts) {

        String userJsonString = null;
        SlashEvent slashEvent = null;
        try {
            DBObject userObject = getSlashObjectByUserId(taskId);
            if (userObject != null) {
                userJsonString = userObject.toString();
                slashEvent = new SlashEvent();
                slashEvent.fromJson(userJsonString);
            }
        } catch (Exception e) {
            logger.error("Error fetching User Preferences details for uid :[{}]  ", taskId, e, e.getMessage());
        }
        return slashEvent;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected DBObject getSlashObjectByUserId(String taskId) {

        DBObject dbObject = null;
        if (mongoDBManager != null) {
            if (StringUtils.isBlank(taskId)) {
                return null;
            }
            Map queryParams = new HashMap<>();
            queryParams.put("taskId", taskId);
            queryParams.put("isActive", true);

            dbObject = mongoDBManager.getObject(FlockConstants.TRAFFIC_DB, queryParams);
        }
        return dbObject;
    }

    protected List<DBObject> getSlashObjectByUserIdTs(String taskId, long ts) {

        List<DBObject> dbObjectList = null;
        if (mongoDBManager != null) {
            if (StringUtils.isBlank(taskId)) {
                return null;
            }
            Map queryParams = new HashMap<>();
            queryParams.put("userId", taskId);
            //queryParams.put("isActive", true);
            queryParams.put("alarmTs", new BasicDBObject("$gte", ts));

            dbObjectList = mongoDBManager.getObjects(FlockConstants.TRAFFIC_DB, queryParams);
        }
        return dbObjectList;
    }
}
