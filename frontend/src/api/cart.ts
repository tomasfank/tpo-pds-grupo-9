import axios from 'axios'
import type { Cart } from '../types'

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

export async function getCart() {
  const { data } = await rivaApi.get<Cart>('/cart')
  return data
}

export async function addCartItem(productId: string, variantId: string, cantidad: number) {
  const { data } = await rivaApi.post<Cart>('/cart/items', {
    productId,
    variantId,
    cantidad,
  })
  return data
}

export async function updateCartItem(itemId: string, cantidad: number) {
  const { data } = await rivaApi.patch<Cart>(
    `/cart/items/${encodeURIComponent(itemId)}`,
    { cantidad },
  )
  return data
}

export async function removeCartItem(itemId: string) {
  const { data } = await rivaApi.delete<Cart>(
    `/cart/items/${encodeURIComponent(itemId)}`,
  )
  return data
}

export async function clearCart() {
  const { data } = await rivaApi.delete<Cart>('/cart/items')
  return data
}
