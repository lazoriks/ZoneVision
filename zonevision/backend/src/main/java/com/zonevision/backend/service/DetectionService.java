package com.zonevision.backend.service;

import com.zonevision.backend.dto.DetectionBatchDto;
import com.zonevision.backend.dto.ZoneEventDto;
import com.zonevision.backend.dto.ZoneUpdateMessage;
import com.zonevision.backend.metrics.ZoneMetrics;
import com.zonevision.backend.model.ZoneEvent;
import com.zonevision.backend.repository.ZoneEventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core ingest pipeline: persists zone events, updates Micrometer/OTel
 * metrics, and pushes a live update to dashboard clients over STOMP.
 */
@Service
public class DetectionService {

    private static final String ZONES_TOPIC = "/topic/zones";

    private final ZoneEventRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ZoneMetrics metrics;
    private final Map<String, Integer> latestOccupancy = new ConcurrentHashMap<>();

    public DetectionService(ZoneEventRepository repository,
                             SimpMessagingTemplate messagingTemplate,
                             ZoneMetrics metrics) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
        this.metrics = metrics;
    }

    public void ingest(DetectionBatchDto batch) {
        metrics.recordLatency(batch.latencyMs());

        for (Map.Entry<String, Integer> entry : batch.occupancy().entrySet()) {
            latestOccupancy.put(entry.getKey(), entry.getValue());
            metrics.updateOccupancy(entry.getKey(), entry.getValue());
        }

        List<ZoneEventDto> events = batch.events();
        for (ZoneEventDto event : events) {
            repository.save(new ZoneEvent(
                    event.trackId(),
                    event.zoneName(),
                    event.eventType(),
                    event.dwellSeconds(),
                    Instant.now()
            ));
            if ("enter".equals(event.eventType())) {
                metrics.recordVisit(event.zoneName());
            }
        }

        ZoneUpdateMessage message = new ZoneUpdateMessage(
                Instant.now(),
                batch.latencyMs(),
                Map.copyOf(latestOccupancy),
                events
        );
        messagingTemplate.convertAndSend(ZONES_TOPIC, message);
    }

    public Map<String, Integer> currentOccupancy() {
        return Map.copyOf(latestOccupancy);
    }

    public List<ZoneEvent> recentEvents() {
        return repository.findTop50ByOrderByOccurredAtDesc();
    }
}
