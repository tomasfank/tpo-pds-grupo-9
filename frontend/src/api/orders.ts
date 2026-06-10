import axios from 'axios'
import type { Order, ShippingAddress } from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const clienteId = 'cliente-demo'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
  headers: {
    'X-Cliente-Id': clienteId,
  },
})

export async function createOrder() {
  const { data } = await rivaApi.post<Order>('/orders')
  return data
}

export async function getOrders() {
  const { data } = await rivaApi.get<Order[]>('/orders')
  return data
}

export async function advanceOrder(orderId: string) {
  const { data } = await rivaApi.post<Order>(
    `/orders/${encodeURIComponent(orderId)}/advance`,
  )
  return data
}

export async function updateOrderShippingAddress(orderId: string, direccionEnvio: ShippingAddress) {
  const { data } = await rivaApi.patch<Order>(
    `/orders/${encodeURIComponent(orderId)}/shipping-address`,
    direccionEnvio,
  )
  return data
}
