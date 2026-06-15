import axios from 'axios'
import type { StoreConfig } from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

// Parametros generales del ecommerce (patron Singleton: Configuracion). Endpoint publico.
export async function getConfig() {
  const { data } = await rivaApi.get<StoreConfig>('/config')
  return data
}
