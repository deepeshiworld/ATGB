package com.flock.atgb.dto;

import com.google.gson.Gson;
import org.json.simple.JSONObject;

/**
 * Created by B0095829 on 4/1/17.
 */
public class FlockUser {

    private static Gson gson = new Gson();

    private String userId;
    private String token;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String toJson() {
        JSONObject obj = toJsonObject();
        return obj.toJSONString();
    }

    @SuppressWarnings("unchecked")
    private JSONObject toJsonObject() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("userId", this.userId);
        jsonObj.put("token", this.token);

        return jsonObj;
    }

    public FlockUser fromJson(String userJsonString) {
        return gson.fromJson(userJsonString, FlockUser.class);
    }

}
