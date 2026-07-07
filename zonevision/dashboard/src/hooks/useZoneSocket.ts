import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { ZoneUpdateMessage } from '../types'

const HTTP_URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080'

/** Connects to the backend's STOMP/SockJS endpoint and subscribes to
 * /topic/zones, exposing the latest ZoneUpdateMessage and connection
 * status to the rest of the app. */
export function useZoneSocket() {
  const [latest, setLatest] = useState<ZoneUpdateMessage | null>(null)
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${HTTP_URL}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        client.subscribe('/topic/zones', (message) => {
          const payload: ZoneUpdateMessage = JSON.parse(message.body)
          setLatest(payload)
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => console.error('STOMP error', frame.headers['message']),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [])

  return { latest, connected }
}
