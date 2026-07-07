import { useEffect, useMemo, useState } from 'react'
import { useZoneSocket } from './hooks/useZoneSocket'
import { fetchInitialOccupancy } from './api'
import { ZoneCard } from './components/ZoneCard'
import { LiveChart } from './components/LiveChart'
import type { OccupancyPoint } from './types'

const MAX_POINTS = 60

export default function App() {
  const { latest, connected } = useZoneSocket()
  const [occupancy, setOccupancy] = useState<Record<string, number>>({})
  const [history, setHistory] = useState<OccupancyPoint[]>([])
  const [latencyMs, setLatencyMs] = useState<number | null>(null)

  useEffect(() => {
    fetchInitialOccupancy().then(setOccupancy).catch(console.error)
  }, [])

  useEffect(() => {
    if (!latest) return
    setOccupancy(latest.occupancy)
    setLatencyMs(latest.latencyMs)
    setHistory((prev) => {
      const point: OccupancyPoint = {
        time: new Date(latest.serverTime).toLocaleTimeString(),
        ...latest.occupancy,
      }
      const next = [...prev, point]
      return next.length > MAX_POINTS ? next.slice(next.length - MAX_POINTS) : next
    })
  }, [latest])

  const zoneNames = useMemo(() => Object.keys(occupancy).sort(), [occupancy])

  return (
    <div className="app">
      <header className="app__header">
        <h1>ZoneVision</h1>
        <span className={`status ${connected ? 'status--live' : 'status--offline'}`}>
          {connected ? 'Live' : 'Connecting…'}
        </span>
        {latencyMs !== null && (
          <span className="latency">detection latency: {latencyMs.toFixed(1)} ms</span>
        )}
      </header>

      <section className="zone-grid">
        {zoneNames.map((zone) => (
          <ZoneCard key={zone} zoneName={zone} occupancy={occupancy[zone]} />
        ))}
      </section>

      <section className="chart-section">
        <LiveChart data={history} zoneNames={zoneNames} />
      </section>
    </div>
  )
}
