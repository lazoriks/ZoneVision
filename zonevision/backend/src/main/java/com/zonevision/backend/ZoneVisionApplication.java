package com.zonevision.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ZoneVision backend entry point.
 *
 * <p>To attach the OpenTelemetry Java agent for auto-instrumentation
 * (traces + JVM/HTTP metrics) alongside the custom Micrometer OTLP
 * metrics exposed by {@link com.zonevision.backend.metrics.ZoneMetrics},
 * run the built jar as:
 *
 * <pre>
 * java -javaagent:/path/to/opentelemetry-javaagent.jar \
 *      -Dotel.service.name=zonevision-backend \
 *      -Dotel.exporter.otlp.endpoint=https://{your-env}.live.dynatrace.com/api/v2/otlp \
 *      -Dotel.exporter.otlp.headers=Authorization=Api-Token%20{your-token} \
 *      -jar target/backend-0.1.0.jar
 * </pre>
 */
@SpringBootApplication
public class ZoneVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZoneVisionApplication.class, args);
    }
}
