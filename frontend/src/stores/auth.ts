import { defineStore } from 'pinia'

type UserInfo = {
  userId: string
  username: string
  displayName: string
  roleId: string
  deptId: string | null
  permissions: string[]
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('zcxt_token') || '',
    user: (localStorage.getItem('zcxt_user') ? JSON.parse(localStorage.getItem('zcxt_user') as string) : null) as UserInfo | null,
  }),
  getters: {
    isAuthed: (s) => Boolean(s.token),
    permissions: (s) => s.user?.permissions || [],
    hasPerm: (s) => (perm: string) => (s.user?.permissions || []).includes(perm) || s.user?.roleId === 'role-super',
  },
  actions: {
    setSession(token: string, user: UserInfo) {
      this.token = token
      this.user = user
      localStorage.setItem('zcxt_token', token)
      localStorage.setItem('zcxt_user', JSON.stringify(user))
    },
    clear() {
      this.token = ''
      this.user = null
      localStorage.removeItem('zcxt_token')
      localStorage.removeItem('zcxt_user')
    },
  },
})
