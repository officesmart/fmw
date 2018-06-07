package com.sharpsec.fmw.location.indoors.model;

public class BodyParams {

    private String eventType;

    public String getEventType() { return this.eventType; }

    public void setEventType(String eventType) { this.eventType = eventType; }

    private EventPayload eventPayload;

    public EventPayload getEventPayload() { return this.eventPayload; }

    public void setEventPayload(EventPayload eventPayload) { this.eventPayload = eventPayload; }
}