<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { TournamentService, PlayerService } from '../services/api'
import type { Tournament } from '../services/api'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'

const router = useRouter()
const tournaments = ref<Tournament[]>([])
const totalGlobalPlayers = ref(0)
const loading = ref(false)
const showCreateDialog = ref(false)
const tournamentName = ref('')
const totalRounds = ref(5)
const submitting = ref(false)

const fetchDashboardData = async () => {
  loading.value = true
  try {
    tournaments.value = await TournamentService.getAll()
    const players = await PlayerService.getAll()
    totalGlobalPlayers.value = players.length
  } catch (err) {
    console.error('Error fetching dashboard data:', err)
  } finally {
    loading.value = false
  }
}

const handleCreateTournament = async () => {
  if (!tournamentName.value.trim() || totalRounds.value <= 0) return
  submitting.value = true
  try {
    const newTournament = await TournamentService.create(
      tournamentName.value.trim(), 
      totalRounds.value
    )
    showCreateDialog.value = false
    tournamentName.value = ''
    totalRounds.value = 5
    // Automatically redirect to the new tournament page to register players
    router.push(`/tournaments/${newTournament.id}`)
  } catch (err) {
    console.error('Error creating tournament:', err)
  } finally {
    submitting.value = false
  }
}

const stats = computed(() => {
  const active = tournaments.value.filter(t => t.status === 'IN_PROGRESS').length
  const draft = tournaments.value.filter(t => t.status === 'DRAFT').length
  const finished = tournaments.value.filter(t => t.status === 'FINISHED').length
  return { active, draft, finished }
})

const getStatusBadgeClass = (status: Tournament['status']) => {
  switch (status) {
    case 'DRAFT': return 'bg-sky-100 text-sky-800 border-sky-200'
    case 'IN_PROGRESS': return 'bg-green-100 text-green-800 border-green-200'
    case 'FINISHED': return 'bg-slate-200 text-slate-700 border-slate-300'
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<template>
  <div class="space-y-8">
    <!-- Quick Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-amber-50 rounded-lg flex items-center justify-center text-amber-500">
          <i class="pi pi-trophy text-2xl"></i>
        </div>
        <div>
          <span class="text-xs text-slate-500 font-semibold uppercase tracking-wider">Active</span>
          <h3 class="text-2xl font-bold text-slate-900">{{ stats.active }}</h3>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-sky-50 rounded-lg flex items-center justify-center text-sky-500">
          <i class="pi pi-file-edit text-2xl"></i>
        </div>
        <div>
          <span class="text-xs text-slate-500 font-semibold uppercase tracking-wider">Drafts</span>
          <h3 class="text-2xl font-bold text-slate-900">{{ stats.draft }}</h3>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-slate-50 rounded-lg flex items-center justify-center text-slate-500">
          <i class="pi pi-check-circle text-2xl"></i>
        </div>
        <div>
          <span class="text-xs text-slate-500 font-semibold uppercase tracking-wider">Finished</span>
          <h3 class="text-2xl font-bold text-slate-900">{{ stats.finished }}</h3>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-emerald-50 rounded-lg flex items-center justify-center text-emerald-500">
          <i class="pi pi-users text-2xl"></i>
        </div>
        <div>
          <span class="text-xs text-slate-500 font-semibold uppercase tracking-wider">Total Players</span>
          <h3 class="text-2xl font-bold text-slate-900">{{ totalGlobalPlayers }}</h3>
        </div>
      </div>
    </div>

    <!-- Tournaments List -->
    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h2 class="text-xl font-bold text-slate-900 mb-1">Chess Tournaments</h2>
          <p class="text-sm text-slate-500">Manage all your local, Swiss-paired chess tournaments.</p>
        </div>
        <Button 
          label="Create Tournament" 
          icon="pi pi-plus" 
          class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 px-4 py-2.5 font-semibold text-sm rounded-lg"
          @click="showCreateDialog = true" 
        />
      </div>

      <div v-if="loading" class="text-center py-12">
        <i class="pi pi-spin pi-spinner text-4xl text-amber-500"></i>
        <p class="mt-2 text-slate-500">Loading tournaments...</p>
      </div>

      <div v-else-if="tournaments.length === 0" class="text-center py-12 text-slate-400">
        <i class="pi pi-trophy text-5xl mb-4 text-slate-300"></i>
        <h3 class="text-lg font-bold text-slate-700">No tournaments yet</h3>
        <p class="text-sm mt-1 mb-4">Click "Create Tournament" to set up your first Swiss-system tournament!</p>
        <Button 
          label="Setup First Tournament" 
          icon="pi pi-plus" 
          class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 px-4 py-2 font-semibold rounded-lg text-sm"
          @click="showCreateDialog = true" 
        />
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div 
          v-for="tournament in tournaments" 
          :key="tournament.id"
          class="bg-slate-50 hover:bg-slate-100/50 transition-all duration-150 p-6 rounded-xl border border-slate-200 cursor-pointer flex flex-col justify-between hover:shadow-md hover:border-amber-400 group"
          @click="router.push(`/tournaments/${tournament.id}`)"
        >
          <div>
            <div class="flex justify-between items-start mb-4">
              <span 
                class="text-xs font-semibold px-2.5 py-1 rounded-full border"
                :class="getStatusBadgeClass(tournament.status)"
              >
                {{ tournament.status }}
              </span>
              <span class="text-xs text-slate-400 font-mono">Rounds: {{ tournament.currentRound }}/{{ tournament.totalRounds }}</span>
            </div>
            <h3 class="text-lg font-bold text-slate-800 group-hover:text-amber-600 transition-colors mb-1">
              {{ tournament.name }}
            </h3>
            <p class="text-xs text-slate-400">
              Created: {{ new Date(tournament.createdAt).toLocaleDateString() }}
            </p>
          </div>
          <div class="mt-6 flex items-center justify-end text-sm text-amber-600 font-semibold gap-1.5 opacity-0 group-hover:opacity-100 transition-opacity">
            Manage <i class="pi pi-arrow-right text-xs"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Tournament Dialog -->
    <Dialog 
      v-model:visible="showCreateDialog" 
      header="Create New Tournament" 
      :modal="true" 
      class="w-full max-w-md bg-white border border-slate-200 rounded-xl overflow-hidden shadow-2xl"
      contentClass="p-6 space-y-4"
    >
      <div class="flex flex-col gap-2">
        <label for="tName" class="text-sm font-semibold text-slate-700">Tournament Name</label>
        <InputText 
          id="tName" 
          v-model="tournamentName" 
          placeholder="E.g., Spring Open 2026" 
          class="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
          @keyup.enter="handleCreateTournament"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label for="rounds" class="text-sm font-semibold text-slate-700">Total Rounds</label>
        <InputNumber 
          id="rounds" 
          v-model="totalRounds" 
          :min="1" 
          :max="12" 
          showButtons
          class="w-full"
          inputClass="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
        />
      </div>

      <template #footer>
        <div class="flex justify-end gap-3 mt-4 border-t border-slate-100 pt-4">
          <Button 
            label="Cancel" 
            class="p-button-text text-slate-500 font-medium py-2 px-4 hover:bg-slate-100 rounded-lg" 
            @click="showCreateDialog = false" 
          />
          <Button 
            label="Create & Open" 
            icon="pi pi-check" 
            :loading="submitting"
            class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-semibold py-2 px-4 rounded-lg" 
            @click="handleCreateTournament" 
          />
        </div>
      </template>
    </Dialog>
  </div>
</template>
