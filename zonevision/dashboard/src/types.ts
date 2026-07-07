export interface ZoneEvent {
  trackId: number
  zoneName: string
  eventType: 'enter' | 'exit'
  dwellSeconds: number | null
}

export interface ZoneUpdateMessage {
  serverTime: string
  latencyMs: number
  occupancy: Record<string, number>
  events: ZoneEvent[]
}

export interface OccupancyPoint {
  time: string
  [zoneName: string]: number | string
}
