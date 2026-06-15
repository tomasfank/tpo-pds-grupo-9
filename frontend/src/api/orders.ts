import axios from 'axios'
import { getToken } from './auth'
import type {
  CheckoutResponse,
  Order,
  PaymentRequest,
  PaymentResponse,
  ShippingAddress,
} from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

// Los pedidos pertenecen al cliente autenticado (CU-18 a CU-22). El avance de
// estado (CU-23) exige rol ADMINISTRADOR; en ambos casos el backend resuelve la
// identidad desde el JWT, que se adjunta aca.
rivaApi.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Patron Facade (CU-18 a CU-20): confirma la compra completa en un paso —
// crea el pedido desde el carrito, procesa el pago y dispara las notificaciones.
export async function checkout(payment: PaymentRequest, direccionEnvio?: ShippingAddress) {
  const { data } = await rivaApi.post<CheckoutResponse>('/orders/checkout', {
    pago: payment,
    direccionEnvio: direccionEnvio ?? null,
  })
  return data
}

export async function getOrders() {
  const { data } = await rivaApi.get<Order[]>('/orders')
  return data
}

// CU-23 — listado de todos los pedidos para el administrador (requiere rol admin).
export async function getAllOrders() {
  const { data } = await rivaApi.get<Order[]>('/orders/admin')
  return data
}

export async function advanceOrder(orderId: string) {
  const { data } = await rivaApi.post<Order>(
    `/orders/${encodeURIComponent(orderId)}/advance`,
  )
  return data
}

export async function processOrderPayment(orderId: string, payment: PaymentRequest) {
  const { data } = await rivaApi.post<PaymentResponse>(
    `/orders/${encodeURIComponent(orderId)}/payment`,
    payment,
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
