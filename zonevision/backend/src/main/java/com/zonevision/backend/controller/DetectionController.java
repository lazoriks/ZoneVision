package com.zonevision.backend.controller;

import com.zonevision.backend.dto.DetectionBatchDto;
import com.zonevision.backend.service.DetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ingest endpoint the Python detector POSTs one batch to per processed frame. */
@RestController
@RequestMapping("/api/detections")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping
    public ResponseEntity<Void> ingest(@RequestBody DetectionBatchDto batch) {
        detectionService.ingest(batch);
        return ResponseEntity.accepted().build();
    }
}
