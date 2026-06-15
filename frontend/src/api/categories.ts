import axios from 'axios'
import { getToken } from './auth'
import type { Category } from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

// CU-13 — las mutaciones de categorías exigen rol ADMINISTRADOR; el GET es público.
// Se adjunta el Bearer cuando hay sesión admin activa.
rivaApi.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export async function getAllCategories() {
  const { data } = await rivaApi.get<Category[]>('/categories')
  return data
}

export async function createCategory(name: string, parentId: string | null) {
  const { data } = await rivaApi.post<Category>('/categories', { name, parentId })
  return data
}

export async function renameCategory(id: string, name: string) {
  const { data } = await rivaApi.put<Category>(`/categories/${encodeURIComponent(id)}`, { name })
  return data
}

export async function moveCategory(id: string, parentId: string | null) {
  const { data } = await rivaApi.put<Category>(
    `/categories/${encodeURIComponent(id)}/parent`,
    { parentId },
  )
  return data
}

export async function deactivateCategory(id: string) {
  const { data } = await rivaApi.delete<Category>(`/categories/${encodeURIComponent(id)}`)
  return data
}

export async function activateCategory(id: string) {
  const { data } = await rivaApi.post<Category>(
    `/categories/${encodeURIComponent(id)}/activate`,
  )
  return data
}
