<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PlayerService } from '../services/api'
import type { Player } from '../services/api'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'

const players = ref<Player[]>([])
const showAddDialog = ref(false)
const playerName = ref('')
const playerRating = ref(1200)
const loading = ref(false)
const submitting = ref(false)

const fetchPlayers = async () => {
  loading.value = true
  try {
    players.value = await PlayerService.getAll()
  } catch (err) {
    console.error('Error fetching players:', err)
  } finally {
    loading.value = false
  }
}

const handleAddPlayer = async () => {
  if (!playerName.value.trim()) return
  submitting.value = true
  try {
    await PlayerService.create(playerName.value.trim(), playerRating.value)
    playerName.value = ''
    playerRating.value = 1200
    showAddDialog.value = false
    await fetchPlayers()
  } catch (err) {
    console.error('Error adding player:', err)
  } finally {
    submitting.value = false
  }
}

const handleDeletePlayer = async (id: string) => {
  if (!confirm('Are you sure you want to delete this player?')) return
  try {
    await PlayerService.delete(id)
    await fetchPlayers()
  } catch (err) {
    console.error('Error deleting player:', err)
  }
}

onMounted(() => {
  fetchPlayers()
})
</script>

<template>
  <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-900 mb-1">Global Player Directory</h2>
        <p class="text-sm text-slate-500">Add, edit, and manage players in the database for tournaments.</p>
      </div>
      <Button 
        label="Add Player" 
        icon="pi pi-plus" 
        class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 px-4 py-2.5 font-semibold text-sm rounded-lg"
        @click="showAddDialog = true" 
      />
    </div>

    <DataTable 
      :value="players" 
      :loading="loading" 
      paginator 
      :rows="10" 
      responsiveLayout="scroll"
      class="p-datatable-sm"
      tableClass="min-w-full"
    >
      <template #empty>
        <div class="text-center py-8 text-slate-400">
          <i class="pi pi-users text-4xl mb-3"></i>
          <p>No players found in database.</p>
        </div>
      </template>
      <Column field="name" header="Player Name" sortable class="p-3 text-slate-700 font-medium"></Column>
      <Column field="rating" header="FIDE / Seeding Rating" sortable class="p-3 text-slate-700">
        <template #body="{ data }">
          <span class="font-mono bg-slate-100 px-2 py-1 rounded text-sm text-slate-700 border border-slate-200">
            {{ data.rating }}
          </span>
        </template>
      </Column>
      <Column header="Actions" class="p-3 text-right">
        <template #body="{ data }">
          <Button 
            icon="pi pi-trash" 
            class="p-button-danger p-button-text p-button-sm hover:text-red-600" 
            @click="handleDeletePlayer(data.id)" 
          />
        </template>
      </Column>
    </DataTable>

    <!-- Add Player Dialog -->
    <Dialog 
      v-model:visible="showAddDialog" 
      header="Create New Player" 
      :modal="true" 
      class="w-full max-w-md bg-white border border-slate-200 rounded-xl overflow-hidden shadow-2xl"
      contentClass="p-6 space-y-4"
    >
      <div class="flex flex-col gap-2">
        <label for="name" class="text-sm font-semibold text-slate-700">Full Name</label>
        <InputText 
          id="name" 
          v-model="playerName" 
          placeholder="E.g., Magnus Carlsen" 
          class="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
          @keyup.enter="handleAddPlayer"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label for="rating" class="text-sm font-semibold text-slate-700">Rating (E.g. FIDE, local)</label>
        <InputNumber 
          id="rating" 
          v-model="playerRating" 
          :min="100" 
          :max="3000" 
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
            @click="showAddDialog = false" 
          />
          <Button 
            label="Save Player" 
            icon="pi pi-check" 
            :loading="submitting"
            class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-semibold py-2 px-4 rounded-lg" 
            @click="handleAddPlayer" 
          />
        </div>
      </template>
    </Dialog>
  </div>
</template>
