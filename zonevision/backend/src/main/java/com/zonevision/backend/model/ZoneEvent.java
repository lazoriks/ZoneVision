package com.zonevision.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Persisted record of a zone enter/exit event. */
@Entity
@Table(name = "zone_events")
public class ZoneEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer trackId;

    @Column(nullable = false)
    private String zoneName;

    @Column(nullable = false)
    private String eventType;

    private Double dwellSeconds;

    @Column(nullable = false)
    private Instant occurredAt;

    protected ZoneEvent() {
        // required by JPA
    }

    public ZoneEvent(Integer trackId, String zoneName, String eventType, Double dwellSeconds, Instant occurredAt) {
        this.trackId = trackId;
        this.zoneName = zoneName;
        this.eventType = eventType;
        this.dwellSeconds = dwellSeconds;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public Integer getTrackId() {
        return trackId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public String getEventType() {
        return eventType;
    }

    public Double getDwellSeconds() {
        return dwellSeconds;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
