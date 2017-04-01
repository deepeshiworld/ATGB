package com.flock.atgb.service;

import java.util.Date;
import java.util.Timer;

/**
 * Created by B0095829 on 4/2/17.
 */
public class TrafficReminder {
    private final static Timer timer = new Timer();

    public static void main(String[] args) {
        Timer timer = new Timer();
        //timer.schedule(new ReminderTask("1"), new Date(1491116451000l));
    }

    public static void addTaskToTimer(ReminderTask task, Date scheduleTime) {
        timer.schedule(task, scheduleTime);
    }

    public static void addTaskToTimer(ReminderTask task, long scheduleTime) {
        timer.schedule(task, scheduleTime);
    }
}
