package com.flock.atgb.dto;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by B0095829 on 4/2/17.
 */
public class SlashEvent {

    private static Gson gson = new Gson();

    private long alarmTs;
    private String chat;
    private String name;
    private String chatName;
    private String text;
    private String userName;
    private String locale;
    private String userId;
    private String command;
    private boolean isActive;
    private long timenTakenSec;
    private String sourceName;
    private String destinationName;
    private String finalTimeToReach;

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getTimenTakenSec() {
        return timenTakenSec;
    }

    public void setTimenTakenSec(long timenTakenSec) {
        this.timenTakenSec = timenTakenSec;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public long getAlarmTs() {
        return alarmTs;
    }

    public void setAlarmTs(long alarmTs) {
        this.alarmTs = alarmTs;
    }

    public String getFinalTimeToReach() {
        return finalTimeToReach;
    }

    public void setFinalTimeToReach(String finalTimeToReach) {
        this.finalTimeToReach = finalTimeToReach;
    }

    public SlashEvent fromJson(String userJsonString) {
        return gson.fromJson(userJsonString, SlashEvent.class);
    }

    public JSONObject toJson() {
        try {
            String toJsonString = gson.toJson(this);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(toJsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
