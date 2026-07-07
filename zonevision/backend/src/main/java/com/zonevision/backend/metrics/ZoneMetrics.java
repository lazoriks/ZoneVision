package com.zonevision.backend.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps the three ZoneVision OTel metrics on top of Micrometer, which
 * ships them to Dynatrace via the OTLP metrics exporter (see
 * application.yml -&gt; management.otlp.metrics.export):
 *
 * <ul>
 *   <li>zonevision.zone.visits (counter, tag: zone_name)</li>
 *   <li>zonevision.detection.latency_ms (gauge)</li>
 *   <li>zonevision.zone.occupancy (gauge, tag: zone_name)</li>
 * </ul>
 */
@Component
public class ZoneMetrics {

    private final MeterRegistry registry;
    private final Map<String, Counter> visitCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> occupancyGauges = new ConcurrentHashMap<>();
    private final AtomicInteger latencyMs = new AtomicInteger(0);

    public ZoneMetrics(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("zonevision.detection.latency_ms", latencyMs);
    }

    public void recordVisit(String zoneName) {
        visitCounters
                .computeIfAbsent(zoneName, name -> Counter.builder("zonevision.zone.visits")
                        .tag("zone_name", name)
                        .register(registry))
                .increment();
    }

    public void updateOccupancy(String zoneName, int count) {
        occupancyGauges
                .computeIfAbsent(zoneName, name -> {
                    AtomicInteger value = new AtomicInteger(0);
                    registry.gauge("zonevision.zone.occupancy", Tags.of("zone_name", name), value);
                    return value;
                })
                .set(count);
    }

    public void recordLatency(double ms) {
        latencyMs.set((int) Math.round(ms));
    }
}
