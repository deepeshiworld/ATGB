package com.flock.atgb.service;

import com.flock.atgb.db.MongoDBManager;
import com.flock.atgb.dto.FlockUser;
import com.flock.atgb.util.FlockConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            logger.error("Error creating MyAirtelAppUserPreferences for : " + flockUser.getUserId() + ". Error : " + e.getMessage(), e);
            return false;
        }
        return true;
    }
}
