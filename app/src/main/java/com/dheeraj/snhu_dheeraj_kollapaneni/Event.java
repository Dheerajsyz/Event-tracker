package com.dheeraj.snhu_dheeraj_kollapaneni;

import java.io.Serializable;

public class Event implements Serializable {
    private String eventId;
    private String userId;
    private String name;
    private String date;
    private String time;
    private String location;

    public Event() {
    }

    public Event(String eventId, String userId, String name, String date, String time, String location) {
        this.eventId = eventId;
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
