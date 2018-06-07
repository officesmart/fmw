package com.sharpsec.fmw.location.indoors;

import java.io.Serializable;

public class LocationEvent implements Serializable {

    public static String LOCATION_EVENT = "LOCATION_EVENT";

    public enum Type { BUILDING, FLOOR, ZONE }
    public enum Action { IN, OUT }

    private String id;
    private long time;
    private Type type;
    private Action action;

    public String getId() { return id; }
    public long getTime() { return time; }
    public Type getType() { return type; }
    public Action getAction() { return action; }

    public LocationEvent(String id, long time, Type type, Action action) {
        this.id = id;
        this.time = time;
        this.type = type;
        this.action = action;
    }
}
