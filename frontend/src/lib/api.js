export const API_BASE = import.meta.env?.VITE_API_BASE || '/api'

export function apiUrl(path) {
  if (!path.startsWith('/')) return `${API_BASE}/${path}`
  return `${API_BASE}${path}`
}

export function withUserId(path, userId) {
  if (!userId) return path
  return path.includes('?') ? `${path}&userId=${userId}` : `${path}?userId=${userId}`
}
