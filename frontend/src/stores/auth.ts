import { defineStore } from 'pinia'
import RemoteService from '@/services/RemoteService'
import type AuthUser from '@/models/AuthUser'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null as string | null,
    user: null as AuthUser | null
  }),

  getters: {
    isAuthenticated(): boolean {
      return !!this.token
    },
    isAdmin(): boolean {
      return this.user?.role === 'ADMINISTRATOR'
    },
    // Usage: authStore.hasPermission('PERSON_DELETE')
    hasPermission(): (permission: string) => boolean {
      return (permission: string) => this.user?.permissions?.includes(permission) ?? false
    }
  },

  actions: {
    async login(email: string, password: string) {
      const res = await RemoteService.login(email, password)
      this.token = res.token
      this.user = res.user
    },

    async fetchMe() {
      if (!this.token) return
      this.user = await RemoteService.getCurrentUser()
    },

    logout() {
      this.token = null
      this.user = null
    }
  },

  persist: true
})
