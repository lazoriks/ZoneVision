package com.zonevision.backend.dto;

/** A single zone enter/exit event, as reported by the Python detector. */
public record ZoneEventDto(
        int trackId,
        String zoneName,
        String eventType,
        Double dwellSeconds
) {}
