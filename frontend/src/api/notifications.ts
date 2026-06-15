import axios from 'axios'
import { getToken } from './auth'
import type { NotificationPreferences } from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

// CU-24 — las preferencias pertenecen al cliente autenticado; el backend las
// resuelve desde el JWT, por eso se adjunta el Bearer en cada request.
rivaApi.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// CU-24 — lee los canales actualmente habilitados para mostrarlos en la config.
export async function getNotificationPreferences(): Promise<NotificationPreferences> {
  const { data } = await rivaApi.get<NotificationPreferences>('/notifications/preferences')
  return data
}

// CU-24 — persiste los canales elegidos. La suscripcion efectiva al pedido
// (patron Observer) se deriva de estas preferencias en el backend.
export async function updateNotificationPreferences(
  preferences: NotificationPreferences,
): Promise<NotificationPreferences> {
  const { data } = await rivaApi.put<NotificationPreferences>(
    '/notifications/preferences',
    preferences,
  )
  return data
}
