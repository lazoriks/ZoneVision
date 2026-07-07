# ZoneVision

Real-time computer-vision zone analytics. A camera feed is run through YOLOv8 + ByteTrack to get stable per-person track IDs, each tracked person is mapped to a named zone (point-in-polygon), and zone enter/exit events plus live occupancy are streamed to a Spring Boot backend, persisted, exposed as OpenTelemetry metrics, and pushed in real time to a React dashboard over WebSocket (STOMP).

```
zonevision/
├── detector/     Python CV service — YOLOv8 + ByteTrack, zone engine, HTTP POST to backend
├── backend/      Spring Boot 3 (Java 21) API — ingest, persistence, WebSocket push, OTel metrics
└── dashboard/    React + TypeScript — live WebSocket client, per-zone cards, live chart
```

## Architecture

```
┌─────────────────┐   POST /api/detections    ┌───────────────────┐   STOMP /topic/zones   ┌──────────────────┐
│  detector/       │ ─────────────────────────▶│  backend/          │ ───────────────────────▶│  dashboard/       │
│  YOLOv8+ByteTrack│   {occupancy, events,      │  Spring Boot 3     │   ZoneUpdateMessage      │  React + TS       │
│  zone_engine.py  │    latencyMs}              │  H2 (in-memory)    │                          │  recharts         │
└─────────────────┘                            │  Micrometer→OTLP   │                          └──────────────────┘
                                                 └─────────┬──────────┘
                                                            │ OTLP (metrics)
                                                            ▼
                                                     Dynatrace (Grail)
```

One HTTP POST is sent per processed video frame. It always carries a latency sample and the full occupancy snapshot, and additionally carries zero or more zone enter/exit events (most frames a tracked person doesn't change zones, so `events` is usually empty).

---

## 1. detector/ — Python CV service

**Stack:** `ultralytics` (YOLOv8 + ByteTrack), OpenCV, `requests`.

### Files

| File | Purpose |
|---|---|
| `zone_engine.py` | `Zone` (named polygon, ray-casting `contains()`), `ZoneTracker` (per-track zone membership, enter/exit events, live occupancy). Pure stdlib — no CV deps, unit-testable in isolation. |
| `detector.py` | Runs `model.track(persist=True, tracker="bytetrack.yaml")` over a video source, feeds centroids into `ZoneTracker`, POSTs one JSON batch per frame to the backend. |
| `zones.json` | Named zone polygons in source-frame pixel coordinates. |
| `requirements.txt` | Python dependencies. |

### Why `track()` and not `predict()`

`model.predict()` re-detects every frame with no memory of identity — the same person gets a new box each frame with no continuity. `model.track(persist=True, tracker="bytetrack.yaml")` runs ByteTrack alongside detection so each person keeps a stable `track_id` across frames, which is required to know when *that specific person* enters or leaves a zone (as opposed to just counting boxes per frame).

### Setup

```bash
cd detector
python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

YOLOv8 weights (`yolov8n.pt` by default) download automatically on first run via `ultralytics`.

### Run

```bash
python detector.py --source path/to/video.mp4 --backend-url http://localhost:8080/api/detections
```

| Flag | Default | Description |
|---|---|---|
| `--source` | `sample.mp4` | Video file path, `0` for the default webcam, or an `rtsp://` URL |
| `--zones` | `zones.json` | Path to the zone polygon config |
| `--backend-url` | `http://localhost:8080/api/detections` | Spring Boot ingest endpoint |
| `--model` | `yolov8n.pt` | YOLOv8 weights (swap for `yolov8s.pt`/`m`/`l` for higher accuracy, more latency) |
| `--conf` | `0.4` | Detection confidence threshold |
| `--show` | off | Opens an annotated video window (press `q` to quit) |
| `--post-timeout` | `2.0` | HTTP POST timeout in seconds |

### Zone config format (`zones.json`)

```json
{
  "zones": [
    { "name": "entrance", "polygon": [[0, 0], [420, 0], [420, 720], [0, 720]] },
    { "name": "checkout", "polygon": [[420, 0], [860, 0], [860, 720], [420, 720]] }
  ]
}
```

Polygon points are `[x, y]` pixel coordinates in the *source video frame*. The shipped example assumes a 1280×720 frame split into three vertical bands — replace with polygons that match your actual camera resolution and layout. Point-in-polygon uses the even-odd (ray-casting) rule, so zones can be arbitrary (non-rectangular) shapes.

### POST payload sent to the backend

```json
{
  "timestamp": 1751879345.12,
  "latencyMs": 18.4,
  "occupancy": { "entrance": 1, "checkout": 0, "aisle": 2 },
  "events": [
    { "trackId": 7, "zoneName": "checkout", "eventType": "enter", "dwellSeconds": null },
    { "trackId": 3, "zoneName": "entrance", "eventType": "exit", "dwellSeconds": 12.6 }
  ]
}
```

---

## 2. backend/ — Spring Boot API (Java 21)

**Stack:** Spring Boot 3.3 (Web, WebSocket/STOMP, Data JPA, Actuator), H2 (in-memory), Micrometer with the OTLP registry, Maven.

### Package layout

```
com.zonevision.backend
├── ZoneVisionApplication.java      Entry point (javadoc documents the OTel Java agent flag)
├── config/
│   ├── WebSocketConfig.java        STOMP broker on /topic, endpoint /ws (SockJS)
│   └── WebConfig.java              CORS for /api/** (dashboard origin)
├── controller/
│   ├── DetectionController.java    POST /api/detections
│   └── ZoneQueryController.java    GET  /api/zones/occupancy, /api/zones/events/recent
├── dto/
│   ├── DetectionBatchDto.java      Incoming payload from detector.py
│   ├── ZoneEventDto.java           One enter/exit event
│   └── ZoneUpdateMessage.java      Outgoing WebSocket push
├── metrics/
│   └── ZoneMetrics.java            Wraps the 3 OTel metrics on Micrometer
├── model/
│   └── ZoneEvent.java              JPA entity persisted to H2
├── repository/
│   └── ZoneEventRepository.java    Spring Data JPA repo
└── service/
    └── DetectionService.java       Ingest pipeline: persist → metrics → WebSocket push
```

### REST API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/detections` | Ingest one frame's batch from the detector. Body: `DetectionBatchDto`. Returns `202 Accepted`. |
| `GET` | `/api/zones/occupancy` | Current occupancy snapshot, `Map<String, Integer>`. Used by the dashboard on first load. |
| `GET` | `/api/zones/events/recent` | Last 50 persisted zone events. |

### WebSocket (STOMP)

- Endpoint: `ws://localhost:8080/ws` (SockJS fallback supported)
- Topic: `/topic/zones` — every ingest publishes a `ZoneUpdateMessage`:

```json
{
  "serverTime": "2026-07-07T14:32:10.512Z",
  "latencyMs": 18.4,
  "occupancy": { "entrance": 1, "checkout": 0, "aisle": 2 },
  "events": [ { "trackId": 7, "zoneName": "checkout", "eventType": "enter", "dwellSeconds": null } ]
}
```

### OpenTelemetry metrics

`ZoneMetrics` exposes exactly the three required metrics via Micrometer, which Spring Boot ships to Dynatrace through the OTLP metrics exporter:

| Metric | Type | Tags | Emitted when |
|---|---|---|---|
| `zonevision.zone.visits` | Counter | `zone_name` | A track's `eventType` is `"enter"` |
| `zonevision.detection.latency_ms` | Gauge | — | Every ingested batch (last frame's processing latency) |
| `zonevision.zone.occupancy` | Gauge | `zone_name` | Every ingested batch, per zone in the occupancy map |

These are separate from the OpenTelemetry **Java agent**, which you attach at JVM launch for auto-instrumentation (HTTP server spans, JVM metrics, etc.) — the two are complementary, not redundant.

### Configuration (`application.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:zonevision;DB_CLOSE_DELAY=-1   # in-memory; data resets on restart
  jpa:
    hibernate:
      ddl-auto: update

management:
  otlp:
    metrics:
      export:
        enabled: ${OTEL_METRICS_ENABLED:false}          # off by default
        url: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/metrics}
        headers:
          Authorization: ${DYNATRACE_API_TOKEN:}
        step: 10s

zonevision:
  cors:
    allowed-origin: ${DASHBOARD_ORIGIN:http://localhost:5173}
```

To send metrics to Dynatrace, set:

```bash
export OTEL_METRICS_ENABLED=true
export OTEL_EXPORTER_OTLP_ENDPOINT=https://{your-environment-id}.live.dynatrace.com/api/v2/otlp/v1/metrics
export DYNATRACE_API_TOKEN="Api-Token dt0c01.XXXX..."
```

### Build & run

```bash
cd backend
mvn spring-boot:run
```

Or build a jar and (optionally) attach the OTel Java agent for auto-instrumented traces alongside the custom metrics above:

```bash
mvn clean package
java -javaagent:/path/to/opentelemetry-javaagent.jar \
     -Dotel.service.name=zonevision-backend \
     -Dotel.exporter.otlp.endpoint=https://{your-env}.live.dynatrace.com/api/v2/otlp \
     -Dotel.exporter.otlp.headers=Authorization=Api-Token%20{your-token} \
     -jar target/backend-0.1.0.jar
```

H2 console (dev only): `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:zonevision`, user `sa`, no password).

---

## 3. dashboard/ — React + TypeScript

**Stack:** Vite, React 18, TypeScript, `@stomp/stompjs` + `sockjs-client`, recharts.

### Files

| File | Purpose |
|---|---|
| `src/hooks/useZoneSocket.ts` | Connects to `/ws` via SockJS/STOMP, subscribes to `/topic/zones`, exposes `{ latest, connected }`. |
| `src/api.ts` | `fetchInitialOccupancy()` — REST call for first paint before the socket connects. |
| `src/components/ZoneCard.tsx` | One card per zone showing live occupancy count. |
| `src/components/LiveChart.tsx` | recharts line chart, one line per zone, rolling 60-point window. |
| `src/App.tsx` | Wires everything together; maintains occupancy state + history buffer. |
| `src/types.ts` | Shared TypeScript types matching the backend DTOs. |

### Setup & run

```bash
cd dashboard
npm install
npm run dev
```

Opens on `http://localhost:5173`. Configure the backend origin via env vars if not running on `localhost:8080`:

```bash
# dashboard/.env.local
VITE_BACKEND_URL=http://localhost:8080
```

---

## End-to-end quick start

```bash
# Terminal 1 — backend
cd backend && mvn spring-boot:run

# Terminal 2 — dashboard
cd dashboard && npm install && npm run dev

# Terminal 3 — detector
cd detector
pip install -r requirements.txt
python detector.py --source path/to/video.mp4 --show
```

Open `http://localhost:5173` — zone cards and the live chart update as the detector processes the video.

## Known limitations / next steps

- `zones.json` polygons are placeholder pixel coordinates — recalibrate against your actual camera resolution.
- H2 is in-memory: zone event history is lost on backend restart. Swap the datasource for Postgres/MySQL if persistence across restarts is needed.
- The detector POSTs synchronously per frame; under sustained backend downtime this adds up to `--post-timeout` seconds of stall per frame. Consider a fire-and-forget async client or a local queue for production use.
- No auth on the ingest endpoint or WebSocket — fine for an MVP/demo, not for exposing the backend beyond localhost/trusted network.
- Dynatrace OTLP export and the OTel Java agent both require real credentials/endpoint to be supplied at runtime (see the `backend/` section above); they're stubbed off by default so the stack runs standalone.
