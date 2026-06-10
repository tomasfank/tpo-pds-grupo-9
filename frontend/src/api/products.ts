import axios from 'axios'
import type { CategoryTreeNode, Product, ProductFilters } from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

function cleanParams(filters: ProductFilters) {
  return Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== undefined && value !== ''),
  )
}

export async function getCatalogTree() {
  const { data } = await rivaApi.get<CategoryTreeNode[]>('/catalog/tree')
  return data
}

export async function getProducts(filters: ProductFilters = {}) {
  const { data } = await rivaApi.get<Product[]>('/products', {
    params: cleanParams(filters),
  })
  return data
}

export async function getProductsByCategory(categoryId: string) {
  const { data } = await rivaApi.get<Product[]>(
    `/catalog/categories/${encodeURIComponent(categoryId)}/products`,
  )
  return data
}

export async function getProduct(productId: string) {
  const { data } = await rivaApi.get<Product>(
    `/products/${encodeURIComponent(productId)}`,
  )
  return data
}
