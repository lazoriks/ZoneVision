import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { OccupancyPoint } from '../types'

interface LiveChartProps {
  data: OccupancyPoint[]
  zoneNames: string[]
}

const COLORS = ['#2563eb', '#16a34a', '#dc2626', '#d97706', '#7c3aed']

export function LiveChart({ data, zoneNames }: LiveChartProps) {
  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="time" />
        <YAxis allowDecimals={false} />
        <Tooltip />
        <Legend />
        {zoneNames.map((zone, i) => (
          <Line
            key={zone}
            type="monotone"
            dataKey={zone}
            stroke={COLORS[i % COLORS.length]}
            dot={false}
            isAnimationActive={false}
          />
        ))}
      </LineChart>
    </ResponsiveContainer>
  )
}
