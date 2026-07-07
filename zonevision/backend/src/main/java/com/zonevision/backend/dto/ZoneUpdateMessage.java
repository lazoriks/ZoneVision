package com.zonevision.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Pushed to dashboard clients over /topic/zones on every ingest. */
public record ZoneUpdateMessage(
        Instant serverTime,
        double latencyMs,
        Map<String, Integer> occupancy,
        List<ZoneEventDto> events
) {}
