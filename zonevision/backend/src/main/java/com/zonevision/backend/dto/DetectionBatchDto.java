package com.zonevision.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * One POST /api/detections payload = one processed video frame from
 * detector.py: a latency sample, the full occupancy snapshot, and any
 * zone enter/exit events that occurred on that frame.
 */
public record DetectionBatchDto(
        double timestamp,
        double latencyMs,
        Map<String, Integer> occupancy,
        List<ZoneEventDto> events
) {}
