package com.fmwrn.indoors.model;

public class EventPayload {

    private String tenantId;
    private String action;
    private String actionTime;
    private String userName;
    private String zoneName;
    private String zoneId;
    private long time;

    public String getTenantId() { return this.tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUserName() { return this.userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getZoneName() { return this.zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public String getZoneId() { return this.zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public long getTime() { return this.time; }
    public void setTime(long time) { this.time = time; }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getActionTime() {
        return actionTime;
    }
    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }
}