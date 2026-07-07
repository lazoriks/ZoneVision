"""
zone_engine.py

Point-in-polygon zone tracking for ZoneVision.

Defines `Zone` (a named polygon) and `ZoneTracker`, which ingests
per-frame tracked person centroids (track_id, center point) and emits
zone enter/exit events plus live per-zone occupancy counts.

Kept dependency-free (stdlib only) so it can be unit tested without
pulling in ultralytics/opencv.
"""
from __future__ import annotations

import json
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple

Point = Tuple[float, float]


@dataclass
class Zone:
    name: str
    polygon: List[Point]

    def contains(self, point: Point) -> bool:
        """Ray-casting point-in-polygon test (even-odd rule)."""
        x, y = point
        inside = False
        n = len(self.polygon)
        x1, y1 = self.polygon[0]
        for i in range(1, n + 1):
            x2, y2 = self.polygon[i % n]
            if y > min(y1, y2):
                if y <= max(y1, y2):
                    if x <= max(x1, x2):
                        if y1 != y2:
                            x_intersect = (y - y1) * (x2 - x1) / (y2 - y1) + x1
                        else:
                            x_intersect = x1
                        if x1 == x2 or x <= x_intersect:
                            inside = not inside
            x1, y1 = x2, y2
        return inside


@dataclass
class TrackState:
    current_zone: Optional[str] = None
    zone_entry_time: Optional[float] = None


@dataclass
class ZoneEvent:
    zone_name: str
    track_id: int
    event_type: str  # "enter" | "exit"
    timestamp: float
    dwell_seconds: Optional[float] = None


class ZoneTracker:
    """Maintains per-track zone membership across frames and emits
    enter/exit events plus live occupancy per zone.

    Usage per frame, per tracked person:
        events = tracker.update(track_id, (cx, cy))
    Once per frame, for any track_id that ByteTrack lost this frame:
        events = tracker.remove_track(track_id)
    """

    def __init__(self, zones_config_path: str):
        self.zones: List[Zone] = self._load_zones(zones_config_path)
        self._track_state: Dict[int, TrackState] = {}
        self._occupancy: Dict[str, int] = {z.name: 0 for z in self.zones}

    @staticmethod
    def _load_zones(path: str) -> List[Zone]:
        data = json.loads(Path(path).read_text())
        return [
            Zone(name=z["name"], polygon=[tuple(p) for p in z["polygon"]])
            for z in data["zones"]
        ]

    def _zone_for_point(self, point: Point) -> Optional[str]:
        for zone in self.zones:
            if zone.contains(point):
                return zone.name
        return None

    def update(self, track_id: int, center: Point) -> List[ZoneEvent]:
        """Call once per tracked person per frame with their bbox
        center. Returns zone enter/exit events triggered by this call
        (usually empty, since most frames a person stays in place)."""
        events: List[ZoneEvent] = []
        now = time.time()
        new_zone = self._zone_for_point(center)
        state = self._track_state.setdefault(track_id, TrackState())

        if new_zone != state.current_zone:
            if state.current_zone is not None:
                dwell = (
                    now - state.zone_entry_time
                    if state.zone_entry_time is not None
                    else None
                )
                self._occupancy[state.current_zone] = max(
                    0, self._occupancy[state.current_zone] - 1
                )
                events.append(
                    ZoneEvent(state.current_zone, track_id, "exit", now, dwell)
                )
            if new_zone is not None:
                self._occupancy[new_zone] += 1
                events.append(ZoneEvent(new_zone, track_id, "enter", now))
                state.zone_entry_time = now
            else:
                state.zone_entry_time = None
            state.current_zone = new_zone

        return events

    def remove_track(self, track_id: int) -> List[ZoneEvent]:
        """Call when a track disappears (lost by ByteTrack) so it
        doesn't linger in occupancy counts forever."""
        events: List[ZoneEvent] = []
        state = self._track_state.pop(track_id, None)
        if state and state.current_zone is not None:
            now = time.time()
            dwell = (
                now - state.zone_entry_time
                if state.zone_entry_time is not None
                else None
            )
            self._occupancy[state.current_zone] = max(
                0, self._occupancy[state.current_zone] - 1
            )
            events.append(
                ZoneEvent(state.current_zone, track_id, "exit", now, dwell)
            )
        return events

    def tracked_ids(self) -> List[int]:
        """IDs currently known to the tracker (in some zone or not)."""
        return list(self._track_state.keys())

    def occupancy(self) -> Dict[str, int]:
        return dict(self._occupancy)
