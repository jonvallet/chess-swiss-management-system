<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { TournamentService, AuthService } from '../services/api'
import { useAuthStore } from '../stores/auth'
import type { Tournament } from '../services/api'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const code = route.params.code as string
const tournament = ref<Tournament | null>(null)
const loading = ref(true)
const errorMsg = ref('')

const playerName = ref('')
const playerRating = ref(1200)
const submitting = ref(false)
const joinError = ref('')

const fetchTournament = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    tournament.value = await TournamentService.getByShareCode(code.toUpperCase().trim())
  } catch (err) {
    console.error('Error fetching tournament by code:', err)
    errorMsg.value = 'Tournament not found. Please verify the code and try again.'
  } finally {
    loading.value = false
  }
}

const handleJoinTournament = async () => {
  if (!tournament.value) return
  if (!playerName.value.trim()) {
    joinError.value = 'Please enter your name.'
    return
  }

  submitting.value = true
  joinError.value = ''
  try {
    const response = await AuthService.joinTournament(
      tournament.value.id,
      playerName.value.trim(),
      playerRating.value || 1200
    )

    authStore.setAuth(response.token, 'PLAYER', response.playerId, response.tournamentId)

    router.push(`/tournaments/${tournament.value.id}`)
  } catch (err: any) {
    console.error('Error joining tournament:', err)
    joinError.value = err.response?.data || 'Could not join tournament. Please try again.'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTournament()
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

    <div v-else-if="tournament" class="max-w-md w-full bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <!-- Tournament Header Banner -->
      <div class="bg-slate-950 text-white p-6 relative">
        <div class="flex items-center gap-2 mb-2">
          <span class="bg-amber-500 text-slate-950 text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-wider">
            Invite
          </span>
          <span class="text-xs text-slate-400 font-mono">CODE: {{ tournament.shareCode }}</span>
        </div>
        <h1 class="text-xl font-extrabold tracking-tight text-white">{{ tournament.name }}</h1>
        <p class="text-xs text-slate-400 mt-1">
          Format: Swiss-system &bull; {{ tournament.totalRounds }} Rounds
        </p>
      </div>

      <!-- Content -->
      <div class="p-6 space-y-6">
        <!-- If already started / finished -->
        <div v-if="tournament.status !== 'DRAFT'" class="space-y-4 text-center py-4">
          <div class="w-12 h-12 bg-amber-50 rounded-full flex items-center justify-center text-amber-500 mx-auto">
            <i class="pi pi-lock text-xl"></i>
          </div>
          <div>
            <h3 class="font-bold text-slate-800">Registration Closed</h3>
            <p class="text-xs text-slate-500 mt-1 max-w-xs mx-auto">
              This tournament has already started ({{ tournament.status }}) and is no longer accepting new players.
            </p>
          </div>
          <Button 
            label="View Standings" 
            icon="pi pi-list"
            class="p-button-outlined border-amber-500 text-amber-600 hover:bg-amber-50 font-semibold px-4 py-2.5 rounded-lg text-sm w-full"
            @click="router.push(`/tournaments/${tournament.id}`)"
          />
        </div>

        <!-- Registration Form -->
        <div v-else class="space-y-5">
          <div class="text-center">
            <h2 class="text-lg font-bold text-slate-800">Join this Tournament</h2>
            <p class="text-xs text-slate-400 mt-0.5">Enter your details to register as a player and join.</p>
          </div>

          <div class="space-y-4">
            <div class="flex flex-col gap-1.5">
              <label for="pName" class="text-xs font-bold text-slate-600 uppercase tracking-wider">Your Full Name</label>
              <InputText 
                id="pName" 
                v-model="playerName" 
                placeholder="E.g., Bobby Fischer" 
                class="w-full px-3 py-2.5 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
                @keyup.enter="handleJoinTournament"
              />
            </div>

            <div class="flex flex-col gap-1.5">
              <label for="pRating" class="text-xs font-bold text-slate-600 uppercase tracking-wider">Your Rating (Optional)</label>
              <InputNumber 
                id="pRating" 
                v-model="playerRating" 
                :min="100" 
                :max="3000" 
                showButtons
                class="w-full"
                inputClass="w-full px-3 py-2.5 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none font-mono"
              />
              <span class="text-[10px] text-slate-400">Leave default (1200) if you don't have an official chess rating.</span>
            </div>
          </div>

          <div v-if="joinError" class="bg-rose-50 text-rose-600 p-3 rounded-lg border border-rose-100 text-xs font-semibold">
            <i class="pi pi-exclamation-circle mr-1"></i> {{ joinError }}
          </div>

          <div class="pt-2 flex flex-col gap-2">
            <Button 
              label="Register & Join Tournament" 
              icon="pi pi-sign-in"
              :loading="submitting"
              class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-extrabold px-5 py-3 rounded-lg text-sm w-full shadow-md"
              @click="handleJoinTournament"
            />
            <Button 
              label="Cancel" 
              class="p-button-text text-slate-400 hover:text-slate-600 font-semibold py-2 rounded-lg text-sm w-full"
              @click="router.push('/')"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
