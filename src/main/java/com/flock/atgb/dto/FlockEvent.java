package com.flock.atgb.dto;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by B0095829 on 4/1/17.
 */
public enum FlockEvent {
    APP_INSTALL("app.install"),
    APP_UNINSTALL("app.uninstall"),
    CHAT_GENERATE_URL_PREVIEW("chat.generateUrlPreview"),
    CHAT_RECEIVE_MESSAGE("chat.receiveMessage"),
    CLIENT_PRESS_BUTTON("client.pressButton"),
    CLIENT_WIDGET_ACTION("client.widgetAction"),
    CLIENT_FLOCKML_ACTION("client.flockmlAction"),
    CLIENT_OPEN_ATTACHMENT_WIDGET("client.openAttachmentWidget"),
    CLIENT_SLASH_COMMAND("client.slashCommand"),
    UNKNOWN("unknown");


    static Map<String, FlockEvent> map = new HashMap<>();

    static {
        for (FlockEvent flockEvent : FlockEvent.values()) {
            map.put(flockEvent.getEventName().toLowerCase(), flockEvent);
        }
    }

    String eventName;

    FlockEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public static FlockEvent getFlockEventByName(String name) {

        if (StringUtils.isBlank(name)) {
            return UNKNOWN;
        }
        return map.get(name.toLowerCase());
    }

}
