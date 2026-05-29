import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const role = ref<string | null>(localStorage.getItem('auth_role'))
  const playerId = ref<string | null>(localStorage.getItem('auth_player_id'))
  const tournamentId = ref<string | null>(localStorage.getItem('auth_tournament_id'))

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isPlayer = computed(() => role.value === 'PLAYER')
  const isViewer = computed(() => role.value === 'VIEWER')
  const canEdit = computed(() => role.value === 'ADMIN' || role.value === 'PLAYER')

  function setAuth(newToken: string, newRole: string, newPlayerId?: string, newTournamentId?: string) {
    token.value = newToken
    role.value = newRole
    playerId.value = newPlayerId || null
    tournamentId.value = newTournamentId || null

    localStorage.setItem('auth_token', newToken)
    localStorage.setItem('auth_role', newRole)
    if (newPlayerId) localStorage.setItem('auth_player_id', newPlayerId)
    if (newTournamentId) localStorage.setItem('auth_tournament_id', newTournamentId)
  }

  function logout() {
    token.value = null
    role.value = null
    playerId.value = null
    tournamentId.value = null

    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_role')
    localStorage.removeItem('auth_player_id')
    localStorage.removeItem('auth_tournament_id')
  }

  return {
    token,
    role,
    playerId,
    tournamentId,
    isAuthenticated,
    isAdmin,
    isPlayer,
    isViewer,
    canEdit,
    setAuth,
    logout
  }
})
