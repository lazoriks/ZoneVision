interface ZoneCardProps {
  zoneName: string
  occupancy: number
}

export function ZoneCard({ zoneName, occupancy }: ZoneCardProps) {
  return (
    <div className="zone-card">
      <h3>{zoneName}</h3>
      <p className="zone-card__count">{occupancy}</p>
      <span className="zone-card__label">people in zone</span>
    </div>
  )
}
