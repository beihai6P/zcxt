import axios from 'axios'
import { useAuthStore } from '../stores/auth'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})

http.interceptors.response.use(
  (r) => r,
  (e) => {
    if (e?.response?.status === 401) {
      const auth = useAuthStore()
      auth.clear()
      location.href = '/login'
    }
    return Promise.reject(e)
  },
)

