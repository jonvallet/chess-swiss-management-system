<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { TournamentService, PlayerService } from '../services/api'
import type { Tournament, TournamentPlayer, Match, PlayerStanding, Player } from '../services/api'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

const route = useRoute()
const tournamentId = computed(() => route.params.id as string)

const tournament = ref<Tournament | null>(null)
const tournamentPlayers = ref<TournamentPlayer[]>([])
const allGlobalPlayers = ref<Player[]>([])
const standings = ref<PlayerStanding[]>([])
const activeTab = ref<'standings' | 'rounds' | 'players'>('standings')
const activeRoundTab = ref(1)

// Round matches map
const roundMatches = ref<Record<number, Match[]>>({})

const loading = ref(false)
const generatingPairings = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    // 1. Fetch tournament info
    tournament.value = await TournamentService.getById(tournamentId.value)
    
    // 2. Fetch tournament registered players
    tournamentPlayers.value = await TournamentService.getPlayers(tournamentId.value)
    
    // 3. Fetch standings
    standings.value = await TournamentService.getStandings(tournamentId.value)

    // 4. Fetch all matches to group them by round
    const allMatches = await TournamentService.getAllMatches(tournamentId.value)
    const grouped: Record<number, Match[]> = {}
    for (let r = 1; r <= tournament.value.totalRounds; r++) {
      grouped[r] = allMatches.filter(m => m.roundNumber === r)
    }
    roundMatches.value = grouped

    // Set active round tab to the tournament's current round (or 1 if draft)
    if (tournament.value.currentRound > 0) {
      activeRoundTab.value = tournament.value.currentRound
    } else {
      activeRoundTab.value = 1
    }

    // 5. Fetch all global players for registration (only if in DRAFT mode)
    if (tournament.value.status === 'DRAFT') {
      const allPlayers = await PlayerService.getAll()
      // Filter out players already registered
      const registeredIds = new Set(tournamentPlayers.value.map(tp => tp.player.id))
      allGlobalPlayers.value = allPlayers.filter(p => !registeredIds.has(p.id))
    }
  } catch (err) {
    console.error('Error fetching tournament details:', err)
  } finally {
    loading.value = false
  }
}

const handleRegisterPlayer = async (playerId: string) => {
  try {
    await TournamentService.registerPlayer(tournamentId.value, playerId)
    await fetchData()
  } catch (err) {
    console.error('Error registering player:', err)
  }
}

const handleGenerateRound = async () => {
  generatingPairings.value = true
  try {
    await TournamentService.generateNextRound(tournamentId.value)
    activeTab.value = 'rounds'
    await fetchData()
  } catch (err) {
    console.error('Error generating pairings:', err)
    alert(err instanceof Error ? err.message : 'Could not generate pairings. Ensure you have an even/odd number of players and they have not all played each other.')
  } finally {
    generatingPairings.value = false
  }
}

const handleSubmitResult = async (matchId: string, result: 'WHITE_WIN' | 'BLACK_WIN' | 'DRAW') => {
  try {
    await TournamentService.submitMatchResult(matchId, result)
    await fetchData()
  } catch (err) {
    console.error('Error submitting result:', err)
  }
}

// Check if all matches in the active round are completed
const isCurrentRoundCompleted = computed(() => {
  if (!tournament.value || tournament.value.currentRound === 0) return false
  const matches = roundMatches.value[tournament.value.currentRound] || []
  if (matches.length === 0) return false
  return matches.every(m => m.result !== 'UNPLAYED')
})

onMounted(() => {
  fetchData()
})

watch(tournamentId, () => {
  fetchData()
})

const copied = ref(false)

const handleCopyInviteLink = () => {
  if (!tournament.value?.shareCode) return
  const link = `${window.location.origin}/join/${tournament.value.shareCode}`
  navigator.clipboard.writeText(link)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}
</script>

<template>
  <div v-if="loading && !tournament" class="text-center py-24">
    <i class="pi pi-spin pi-spinner text-4xl text-amber-500"></i>
    <p class="mt-2 text-slate-500">Loading tournament details...</p>
  </div>

  <div v-else-if="tournament" class="space-y-6">
    <!-- Tournament Summary Bar -->
    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
      <div>
        <div class="flex items-center gap-3 mb-1">
          <span 
            class="text-xs font-semibold px-2.5 py-1 rounded-full border"
            :class="
              tournament.status === 'DRAFT' ? 'bg-sky-100 text-sky-800 border-sky-200' :
              tournament.status === 'IN_PROGRESS' ? 'bg-green-100 text-green-800 border-green-200' :
              'bg-slate-200 text-slate-700 border-slate-300'
            "
          >
            {{ tournament.status }}
          </span>
          <span class="text-sm text-slate-400 font-mono">
            Rounds: {{ tournament.currentRound }} of {{ tournament.totalRounds }}
          </span>
        </div>
        <h2 class="text-2xl font-bold text-slate-900">{{ tournament.name }}</h2>
        <div v-if="tournament.shareCode" class="mt-2 flex items-center gap-2 text-xs text-slate-500">
          <span class="font-semibold uppercase tracking-wider">Invite Link:</span>
          <span class="font-mono bg-slate-100 border border-slate-200 text-slate-700 px-2 py-0.5 rounded font-bold">
            {{ tournament.shareCode }}
          </span>
          <Button 
            :label="copied ? 'Copied!' : 'Copy Link'" 
            :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
            class="p-button-text p-button-xs py-0.5 px-2 text-amber-600 hover:text-amber-700 font-bold border border-transparent hover:border-amber-200 hover:bg-amber-50 rounded"
            @click="handleCopyInviteLink"
          />
        </div>
      </div>

      <!-- Action Button -->
      <div class="w-full md:w-auto">
        <!-- Draft Mode -> Generate Round 1 -->
        <Button 
          v-if="tournament.status === 'DRAFT'"
          label="Generate Round 1 Pairings"
          icon="pi pi-play"
          :disabled="tournamentPlayers.length < 2 || generatingPairings"
          :loading="generatingPairings"
          class="w-full md:w-auto bg-green-500 hover:bg-green-600 border-none text-white font-semibold px-5 py-3 rounded-lg text-sm shadow-md"
          @click="handleGenerateRound"
        />

        <!-- Active round matches finished -> Generate Next Round -->
        <Button 
          v-else-if="tournament.status === 'IN_PROGRESS' && isCurrentRoundCompleted && tournament.currentRound < tournament.totalRounds"
          :label="`Generate Round ${tournament.currentRound + 1} Pairings`"
          icon="pi pi-forward"
          :loading="generatingPairings"
          class="w-full md:w-auto bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-semibold px-5 py-3 rounded-lg text-sm shadow-md"
          @click="handleGenerateRound"
        />

        <!-- Active round matches in progress but uncompleted -->
        <div 
          v-else-if="tournament.status === 'IN_PROGRESS' && !isCurrentRoundCompleted"
          class="bg-amber-50 text-amber-700 px-4 py-2.5 rounded-lg border border-amber-200 text-sm font-semibold flex items-center gap-2"
        >
          <i class="pi pi-info-circle"></i>
          Complete all round {{ tournament.currentRound }} matches to continue
        </div>

        <!-- Finished -->
        <div 
          v-else-if="tournament.status === 'FINISHED'"
          class="bg-emerald-50 text-emerald-700 px-5 py-3 rounded-lg border border-emerald-200 text-sm font-bold flex items-center gap-2 shadow-sm"
        >
          <i class="pi pi-check-circle text-lg"></i>
          Tournament Completed
        </div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="flex border-b border-slate-200 bg-white px-6 rounded-t-xl">
      <button 
        class="py-4 px-6 font-semibold text-sm border-b-2 transition-all duration-150"
        :class="activeTab === 'standings' ? 'border-amber-500 text-amber-600' : 'border-transparent text-slate-500 hover:text-slate-700'"
        @click="activeTab = 'standings'"
      >
        <i class="pi pi-list mr-1.5"></i> Standings
      </button>
      <button 
        class="py-4 px-6 font-semibold text-sm border-b-2 transition-all duration-150"
        :class="activeTab === 'rounds' ? 'border-amber-500 text-amber-600' : 'border-transparent text-slate-500 hover:text-slate-700'"
        @click="activeTab = 'rounds'"
      >
        <i class="pi pi-calendar mr-1.5"></i> Pairings & Rounds
      </button>
      <button 
        v-if="tournament.status === 'DRAFT'"
        class="py-4 px-6 font-semibold text-sm border-b-2 transition-all duration-150"
        :class="activeTab === 'players' ? 'border-amber-500 text-amber-600' : 'border-transparent text-slate-500 hover:text-slate-700'"
        @click="activeTab = 'players'"
      >
        <i class="pi pi-user-plus mr-1.5"></i> Register Players ({{ tournamentPlayers.length }})
      </button>
    </div>

    <!-- Tab Panels -->
    <div class="bg-white p-6 rounded-b-xl shadow-sm border-x border-b border-slate-200 min-h-[400px]">
      
      <!-- 1. STANDINGS PANEL -->
      <div v-if="activeTab === 'standings'" class="space-y-4">
        <DataTable :value="standings" class="p-datatable-sm" responsiveLayout="scroll">
          <template #empty>
            <div class="text-center py-12 text-slate-400">
              <i class="pi pi-list text-4xl mb-3"></i>
              <p>No games played yet. Register players and start Round 1!</p>
            </div>
          </template>
          <Column field="rank" header="Rank" class="p-3 font-semibold text-slate-700"></Column>
          <Column field="playerName" header="Player" class="p-3 font-medium text-slate-800"></Column>
          <Column field="rating" header="Rating" class="p-3 font-mono text-slate-500"></Column>
          <Column field="score" header="Points" class="p-3 text-slate-900 font-bold">
            <template #body="{ data }">
              <span class="bg-amber-50 border border-amber-200 text-amber-800 font-bold px-2.5 py-1 rounded text-sm">
                {{ data.score }} pts
              </span>
            </template>
          </Column>
          <Column field="buchholz" header="Buchholz (BH)" class="p-3 text-slate-600 font-mono"></Column>
          <Column field="colorDifference" header="Color Diff" class="p-3 font-mono text-xs">
            <template #body="{ data }">
              <span :class="data.colorDifference > 0 ? 'text-blue-600' : data.colorDifference < 0 ? 'text-rose-600' : 'text-slate-500'">
                {{ data.colorDifference > 0 ? '+' : '' }}{{ data.colorDifference }}
              </span>
            </template>
          </Column>
        </DataTable>
      </div>

      <!-- 2. PAIRINGS & ROUNDS PANEL -->
      <div v-else-if="activeTab === 'rounds'" class="space-y-6">
        <!-- Sub-tabs for each active/completed round -->
        <div v-if="tournament.currentRound > 0" class="flex gap-2 border-b border-slate-100 pb-3 overflow-x-auto">
          <button 
            v-for="r in tournament.currentRound" 
            :key="r"
            class="px-4 py-2 text-xs font-bold rounded-lg border transition-all"
            :class="activeRoundTab === r 
              ? 'bg-slate-800 text-white border-slate-800 shadow' 
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'"
            @click="activeRoundTab = r"
          >
            Round {{ r }}
          </button>
        </div>

        <div v-if="tournament.currentRound === 0" class="text-center py-12 text-slate-400">
          <i class="pi pi-calendar text-5xl mb-3 text-slate-300"></i>
          <h3 class="text-lg font-semibold text-slate-600">No rounds generated</h3>
          <p class="text-sm">Enroll players and click "Generate Round 1 Pairings" above to launch the tournament!</p>
        </div>

        <div v-else class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div 
              v-for="match in roundMatches[activeRoundTab]" 
              :key="match.id"
              class="bg-slate-50 border p-5 rounded-xl flex flex-col justify-between"
              :class="match.result !== 'UNPLAYED' ? 'border-slate-200 bg-slate-50/40' : 'border-slate-300 shadow-xs'"
            >
              <!-- Player Row Details -->
              <div class="space-y-4">
                <!-- White Player -->
                <div class="flex justify-between items-center">
                  <div class="flex items-center gap-2">
                    <span class="w-5 h-5 bg-white border border-slate-300 rounded shadow-xs inline-block"></span>
                    <span class="font-bold text-slate-800 text-sm">{{ match.whitePlayer?.name }}</span>
                    <span class="text-xs text-slate-400 font-mono">({{ match.whitePlayer?.rating }})</span>
                  </div>
                  <span 
                    v-if="match.result !== 'UNPLAYED'"
                    class="font-extrabold text-sm"
                    :class="match.result === 'WHITE_WIN' ? 'text-amber-600' : match.result === 'BLACK_WIN' ? 'text-slate-400' : 'text-slate-500'"
                  >
                    {{ match.result === 'WHITE_WIN' ? '1.0' : match.result === 'BLACK_WIN' ? '0.0' : '0.5' }}
                  </span>
                </div>

                <!-- Divider / VS -->
                <div class="h-px bg-slate-200 relative my-2">
                  <span class="absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bg-slate-50 px-2 text-[10px] text-slate-400 font-bold uppercase tracking-widest">vs</span>
                </div>

                <!-- Black Player / Bye -->
                <div class="flex justify-between items-center">
                  <div class="flex items-center gap-2">
                    <span class="w-5 h-5 bg-slate-900 border border-slate-950 rounded shadow-xs inline-block"></span>
                    <span v-if="match.isBye" class="font-semibold text-slate-400 text-sm italic">BYE (No Opponent)</span>
                    <span v-else class="font-bold text-slate-800 text-sm">{{ match.blackPlayer?.name }}</span>
                    <span v-if="!match.isBye" class="text-xs text-slate-400 font-mono">({{ match.blackPlayer?.rating }})</span>
                  </div>
                  <span 
                    v-if="match.result !== 'UNPLAYED'"
                    class="font-extrabold text-sm"
                    :class="match.result === 'BLACK_WIN' ? 'text-amber-600' : match.result === 'WHITE_WIN' ? 'text-slate-400' : 'text-slate-500'"
                  >
                    {{ match.result === 'BLACK_WIN' ? '1.0' : match.result === 'WHITE_WIN' ? '0.0' : '0.5' }}
                  </span>
                </div>
              </div>

              <!-- Input Actions for UNPLAYED or active matches -->
              <div class="mt-5 border-t border-slate-200/60 pt-4 flex flex-col gap-2">
                <span class="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                  {{ match.result !== 'UNPLAYED' ? 'Change Result' : 'Enter Match Outcome' }}
                </span>
                
                <div v-if="match.isBye" class="text-xs text-green-600 bg-green-50 px-3 py-2 rounded-lg border border-green-150 font-semibold">
                  <i class="pi pi-check mr-1"></i> BYE award of 1.0 point saved automatically.
                </div>

                <div v-else class="grid grid-cols-3 gap-2">
                  <Button 
                    label="White Win" 
                    class="py-1.5 px-2 text-xs font-bold border rounded-lg transition-all"
                    :class="match.result === 'WHITE_WIN'
                      ? 'bg-slate-800 text-white border-slate-800' 
                      : 'bg-white text-slate-700 hover:bg-slate-100 border-slate-200'"
                    @click="handleSubmitResult(match.id, 'WHITE_WIN')"
                  />
                  <Button 
                    label="Draw" 
                    class="py-1.5 px-2 text-xs font-bold border rounded-lg transition-all"
                    :class="match.result === 'DRAW'
                      ? 'bg-slate-800 text-white border-slate-800' 
                      : 'bg-white text-slate-700 hover:bg-slate-100 border-slate-200'"
                    @click="handleSubmitResult(match.id, 'DRAW')"
                  />
                  <Button 
                    label="Black Win" 
                    class="py-1.5 px-2 text-xs font-bold border rounded-lg transition-all"
                    :class="match.result === 'BLACK_WIN'
                      ? 'bg-slate-800 text-white border-slate-800' 
                      : 'bg-white text-slate-700 hover:bg-slate-100 border-slate-200'"
                    @click="handleSubmitResult(match.id, 'BLACK_WIN')"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. PLAYERS REGISTER PANEL -->
      <div v-else-if="activeTab === 'players'" class="grid grid-cols-1 md:grid-cols-2 gap-8">
        
        <!-- Currently Registered List -->
        <div class="bg-slate-50 p-6 rounded-xl border border-slate-200">
          <h3 class="font-bold text-slate-800 mb-4 flex justify-between items-center">
            <span>Enrolled Players</span>
            <span class="bg-slate-200 text-slate-700 px-2.5 py-0.5 rounded-full text-xs font-semibold">
              {{ tournamentPlayers.length }} Enrolled
            </span>
          </h3>
          <div v-if="tournamentPlayers.length === 0" class="text-center py-12 text-slate-400">
            <i class="pi pi-users text-3xl mb-2"></i>
            <p class="text-xs">No players enrolled in this tournament draft yet.</p>
          </div>
          <ul v-else class="space-y-2 max-h-[400px] overflow-y-auto pr-2">
            <li 
              v-for="tp in tournamentPlayers" 
              :key="tp.player.id"
              class="bg-white px-4 py-3 rounded-lg border border-slate-200 flex justify-between items-center"
            >
              <div class="flex items-center gap-2">
                <span class="font-semibold text-slate-700 text-sm">{{ tp.player.name }}</span>
                <span class="text-xs text-slate-400 font-mono">({{ tp.player.rating }})</span>
              </div>
            </li>
          </ul>
        </div>

        <!-- Available Players List -->
        <div class="p-6 bg-white border border-slate-200 rounded-xl">
          <h3 class="font-bold text-slate-800 mb-4">Add Players from Directory</h3>
          <div v-if="allGlobalPlayers.length === 0" class="text-center py-12 text-slate-400">
            <i class="pi pi-user-plus text-3xl mb-2"></i>
            <p class="text-xs">All database players are enrolled, or there are no players in the global directory.</p>
            <router-link to="/players" class="mt-3 inline-block text-xs font-bold text-amber-600 hover:underline">Go to Player Directory &rarr;</router-link>
          </div>
          <ul v-else class="space-y-2 max-h-[400px] overflow-y-auto pr-2">
            <li 
              v-for="p in allGlobalPlayers" 
              :key="p.id"
              class="bg-slate-50 px-4 py-3 rounded-lg border border-slate-200 flex justify-between items-center"
            >
              <div class="flex items-center gap-2">
                <span class="font-semibold text-slate-700 text-sm">{{ p.name }}</span>
                <span class="text-xs text-slate-400 font-mono">({{ p.rating }})</span>
              </div>
              <Button 
                label="Enroll" 
                icon="pi pi-plus" 
                class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 text-xs font-bold px-3 py-1.5 rounded"
                @click="handleRegisterPlayer(p.id)"
              />
            </li>
          </ul>
        </div>
      </div>

    </div>
  </div>
</template>
