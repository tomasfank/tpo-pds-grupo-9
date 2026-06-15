import axios from 'axios'
import { getToken } from './auth'
import type {
  CategoryTreeNode,
  CreateProductPayload,
  Product,
  ProductFilters,
  UpdateProductPayload,
} from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

// Las lecturas del catalogo son publicas; las operaciones admin (CU-10/11/12)
// requieren JWT. Se adjunta el Bearer cuando hay sesion activa.
rivaApi.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
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

// CU-10 — alta de producto (solo Administrador). El backend exige rol
// ADMINISTRADOR; el JWT se adjunta via interceptor.
export async function createProduct(payload: CreateProductPayload) {
  const { data } = await rivaApi.post<Product>('/products', payload)
  return data
}

// CU-11 — edicion de producto (solo Administrador). El backend solo aplica los
// campos no-null y recalcula la cadena de categorias si cambia categoryId.
export async function updateProduct(productId: string, payload: UpdateProductPayload) {
  const { data } = await rivaApi.put<Product>(
    `/products/${encodeURIComponent(productId)}`,
    payload,
  )
  return data
}

// CU-12 — desactivacion de producto (solo Administrador). El producto deja de ser
// visible en el catalogo publico pero sigue referenciable desde pedidos historicos.
export async function deactivateProduct(productId: string) {
  const { data } = await rivaApi.delete<Product>(
    `/products/${encodeURIComponent(productId)}`,
  )
  return data
}
