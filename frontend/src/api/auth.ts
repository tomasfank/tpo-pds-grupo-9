import axios from 'axios'
import type { AuthSession, Rol } from '../types'

const apiUrl =
  import.meta.env.VITE_API_URL ??
  import.meta.env.REACT_APP_API_URL ??
  'http://localhost:8080'

const rivaApi = axios.create({
  baseURL: `${apiUrl}/api`,
})

const SESSION_KEY = 'riva.auth.session'

type LoginResponse = {
  token: string
  rol: Rol
  nombre: string
  apellido: string
  email: string
}

// CU-02 / CU-03 — inicio de sesion contra POST /api/auth/login.
// El backend devuelve el JWT mas los datos basicos del usuario y su rol.
export async function login(email: string, password: string): Promise<AuthSession> {
  const { data } = await rivaApi.post<LoginResponse>('/auth/login', { email, password })
  return {
    token: data.token,
    rol: data.rol,
    nombre: data.nombre,
    apellido: data.apellido,
    email: data.email,
  }
}

// CU-04 — cierre de sesion. Con JWT stateless basta con descartar el token local;
// se avisa al backend de forma best-effort (el endpoint es publico y tolera token expirado).
export async function logout(): Promise<void> {
  try {
    await rivaApi.post('/auth/logout')
  } catch {
    // El logout local no depende de la respuesta del backend.
  }
}

export function getSession(): AuthSession | null {
  const raw = window.localStorage.getItem(SESSION_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as AuthSession
  } catch {
    window.localStorage.removeItem(SESSION_KEY)
    return null
  }
}

export function setSession(session: AuthSession): void {
  window.localStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession(): void {
  window.localStorage.removeItem(SESSION_KEY)
}

export function getToken(): string | null {
  return getSession()?.token ?? null
}
