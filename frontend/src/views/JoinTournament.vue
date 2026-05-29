<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { TournamentService, AuthService } from '../services/api'
import { useAuthStore } from '../stores/auth'
import type { Tournament } from '../services/api'
import Button from 'primevue/button'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const code = route.params.code as string
const tournament = ref<Tournament | null>(null)
const loading = ref(true)
const errorMsg = ref('')

const fetchAndJoin = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const normalizedCode = code.toUpperCase().trim()
    tournament.value = await TournamentService.getByShareCode(normalizedCode)

    const response = await AuthService.viewTournament(normalizedCode)

    authStore.setAuth(response.token, 'CONTROLLER', undefined, response.tournamentId)

    router.replace(`/tournaments/${response.tournamentId}`)
  } catch (err) {
    console.error('Error accessing tournament:', err)
    errorMsg.value = 'Tournament not found. Please verify the code and try again.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAndJoin()
})
</script>

<template>
  <div class="min-h-[70vh] flex flex-col items-center justify-center p-4">
    <div v-if="loading" class="text-center py-12">
      <i class="pi pi-spin pi-spinner text-4xl text-amber-500"></i>
      <p class="mt-2 text-slate-500">Retrieving tournament details...</p>
    </div>

    <div v-else-if="errorMsg" class="max-w-md w-full bg-white p-8 rounded-2xl shadow-sm border border-slate-200 text-center space-y-6">
      <div class="w-16 h-16 bg-rose-50 rounded-full flex items-center justify-center text-rose-500 mx-auto">
        <i class="pi pi-exclamation-triangle text-3xl"></i>
      </div>
      <div>
        <h2 class="text-xl font-bold text-slate-900 mb-2">Tournament Not Found</h2>
        <p class="text-sm text-slate-500">{{ errorMsg }}</p>
      </div>
      <Button 
        label="Back to Dashboard" 
        icon="pi pi-home"
        class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-semibold px-4 py-2.5 rounded-lg text-sm w-full"
        @click="router.push('/')"
      />
    </div>
  </div>
</template>
