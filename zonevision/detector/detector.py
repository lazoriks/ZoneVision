"""
detector.py

YOLOv8 + ByteTrack person detection with real-time zone analytics.

Uses model.track(persist=True, tracker="bytetrack.yaml") -- NOT
model.predict() -- so each detected person keeps a stable track_id
across frames. Per frame, tracked centroids are fed into ZoneTracker
to produce zone enter/exit events and live occupancy, which are then
POSTed as a single batch to the Spring Boot backend.

Example:
    python detector.py --source sample.mp4 --backend-url http://localhost:8080/api/detections
"""
from __future__ import annotations

import argparse
import logging
import time

import cv2
import requests
from ultralytics import YOLO

from zone_engine import ZoneTracker

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s"
)
log = logging.getLogger("zonevision.detector")

PERSON_CLASS_ID = 0  # COCO class 0 = person


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="ZoneVision detector")
    parser.add_argument(
        "--source",
        default="sample.mp4",
        help="Video source: file path, '0' for default webcam, or an rtsp:// URL",
    )
    parser.add_argument(
        "--zones", default="zones.json", help="Path to zones config JSON"
    )
    parser.add_argument(
        "--backend-url",
        default="http://localhost:8080/api/detections",
        help="Spring Boot ingest endpoint",
    )
    parser.add_argument("--model", default="yolov8n.pt", help="YOLOv8 weights")
    parser.add_argument(
        "--conf", type=float, default=0.4, help="Detection confidence threshold"
    )
    parser.add_argument(
        "--show", action="store_true", help="Show annotated video window"
    )
    parser.add_argument(
        "--post-timeout", type=float, default=2.0, help="HTTP POST timeout (seconds)"
    )
    return parser.parse_args()


def post_batch(backend_url: str, payload: dict, timeout: float) -> None:
    try:
        requests.post(backend_url, json=payload, timeout=timeout)
    except requests.RequestException as exc:
        log.warning("Failed to POST to backend: %s", exc)


def main() -> None:
    args = parse_args()
    source = 0 if args.source == "0" else args.source

    model = YOLO(args.model)
    tracker = ZoneTracker(args.zones)

    log.info("Starting detector on source=%s backend=%s", source, args.backend_url)

    for result in model.track(
        source=source,
        conf=args.conf,
        classes=[PERSON_CLASS_ID],
        tracker="bytetrack.yaml",
        persist=True,
        stream=True,
        verbose=False,
    ):
        frame_start = time.time()
        active_track_ids = set()
        frame_events = []

        boxes = result.boxes
        if boxes is not None and boxes.id is not None:
            for box, track_id in zip(
                boxes.xyxy.cpu().numpy(), boxes.id.cpu().numpy()
            ):
                x1, y1, x2, y2 = box
                center = ((x1 + x2) / 2, (y1 + y2) / 2)
                tid = int(track_id)
                active_track_ids.add(tid)
                frame_events.extend(tracker.update(tid, center))

        # Tracks ByteTrack lost this frame no longer occupy a zone.
        stale_ids = set(tracker.tracked_ids()) - active_track_ids
        for tid in stale_ids:
            frame_events.extend(tracker.remove_track(tid))

        latency_ms = (time.time() - frame_start) * 1000

        payload = {
            "timestamp": time.time(),
            "latencyMs": round(latency_ms, 2),
            "occupancy": tracker.occupancy(),
            "events": [
                {
                    "trackId": e.track_id,
                    "zoneName": e.zone_name,
                    "eventType": e.event_type,
                    "dwellSeconds": (
                        round(e.dwell_seconds, 2)
                        if e.dwell_seconds is not None
                        else None
                    ),
                }
                for e in frame_events
            ],
        }
        post_batch(args.backend_url, payload, args.post_timeout)
        log.debug(
            "Frame processed in %.1fms, occupancy=%s", latency_ms, tracker.occupancy()
        )

        if args.show:
            annotated = result.plot()
            cv2.imshow("ZoneVision", annotated)
            if cv2.waitKey(1) & 0xFF == ord("q"):
                break

    if args.show:
        cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
