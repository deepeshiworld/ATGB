package com.flock.atgb.service;

import com.flock.atgb.dto.FlockUser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            return flockDbService.addUserInDb(flockUser);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
