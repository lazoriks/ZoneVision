const HTTP_URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080'

export async function fetchInitialOccupancy(): Promise<Record<string, number>> {
  const res = await fetch(`${HTTP_URL}/api/zones/occupancy`)
  if (!res.ok) {
    throw new Error(`Failed to load occupancy: ${res.status}`)
  }
  return res.json()
}
