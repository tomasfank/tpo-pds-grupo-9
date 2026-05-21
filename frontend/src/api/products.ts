import axios from 'axios'
import type { ApiCategory, Product } from '../types'

export const FAKE_STORE_API_URL = 'https://fakestoreapi.com'

const productsApi = axios.create({
  baseURL: FAKE_STORE_API_URL,
})

export async function getProducts() {
  const { data } = await productsApi.get<Product[]>('/products')
  return data
}

export async function getProductsByCategory(category: ApiCategory) {
  const { data } = await productsApi.get<Product[]>(
    `/products/category/${encodeURIComponent(category)}`,
  )

  return data
}
