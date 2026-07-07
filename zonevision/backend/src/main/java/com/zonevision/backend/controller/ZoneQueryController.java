package com.zonevision.backend.controller;

import com.zonevision.backend.model.ZoneEvent;
import com.zonevision.backend.service.DetectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** REST reads for dashboard initial page load (before the WebSocket connects). */
@RestController
@RequestMapping("/api/zones")
public class ZoneQueryController {

    private final DetectionService detectionService;

    public ZoneQueryController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping("/occupancy")
    public Map<String, Integer> occupancy() {
        return detectionService.currentOccupancy();
    }

    @GetMapping("/events/recent")
    public List<ZoneEvent> recentEvents() {
        return detectionService.recentEvents();
    }
}
