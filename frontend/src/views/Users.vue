<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { AdminService } from '../services/api'
import type { UserResponse } from '../services/api'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'

const users = ref<UserResponse[]>([])
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const editingUser = ref<UserResponse | null>(null)
const newUsername = ref('')
const newPassword = ref('')
const editPassword = ref('')
const loading = ref(false)
const submitting = ref(false)

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await AdminService.getUsers()
  } catch (err) {
    console.error('Error fetching users:', err)
  } finally {
    loading.value = false
  }
}

const handleAddUser = async () => {
  if (!newUsername.value.trim() || !newPassword.value.trim()) return
  submitting.value = true
  try {
    await AdminService.createUser(newUsername.value.trim(), newPassword.value)
    newUsername.value = ''
    newPassword.value = ''
    showAddDialog.value = false
    await fetchUsers()
  } catch (err) {
    console.error('Error adding user:', err)
  } finally {
    submitting.value = false
  }
}

const handleEditUser = (user: UserResponse) => {
  editingUser.value = user
  editPassword.value = ''
  showEditDialog.value = true
}

const handleUpdatePassword = async () => {
  if (!editingUser.value || !editPassword.value.trim()) return
  submitting.value = true
  try {
    await AdminService.updatePassword(editingUser.value.id, editPassword.value)
    editPassword.value = ''
    showEditDialog.value = false
    editingUser.value = null
    await fetchUsers()
  } catch (err) {
    console.error('Error updating user:', err)
  } finally {
    submitting.value = false
  }
}

const handleDeleteUser = async (id: string) => {
  if (!confirm('Are you sure you want to delete this user?')) return
  try {
    await AdminService.deleteUser(id)
    await fetchUsers()
  } catch (err) {
    console.error('Error deleting user:', err)
  }
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-900 mb-1">Admin Users</h2>
        <p class="text-sm text-slate-500">Manage administrator accounts for the system.</p>
      </div>
      <Button 
        label="Add User" 
        icon="pi pi-plus" 
        class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 px-4 py-2.5 font-semibold text-sm rounded-lg"
        @click="showAddDialog = true" 
      />
    </div>

    <DataTable 
      :value="users" 
      :loading="loading" 
      paginator 
      :rows="10" 
      responsiveLayout="scroll"
      class="p-datatable-sm"
      tableClass="min-w-full"
    >
      <template #empty>
        <div class="text-center py-8 text-slate-400">
          <i class="pi pi-shield text-4xl mb-3"></i>
          <p>No admin users found.</p>
        </div>
      </template>
      <Column field="username" header="Username" sortable class="p-3 text-slate-700 font-medium"></Column>
      <Column field="role" header="Role" sortable class="p-3 text-slate-700">
        <template #body="{ data }">
          <span class="bg-amber-100 text-amber-800 px-2 py-1 rounded text-sm font-medium border border-amber-200">
            {{ data.role }}
          </span>
        </template>
      </Column>
      <Column field="createdAt" header="Created" sortable class="p-3 text-slate-500 text-sm">
        <template #body="{ data }">
          {{ formatDate(data.createdAt) }}
        </template>
      </Column>
      <Column header="Actions" class="p-3 text-right">
        <template #body="{ data }">
          <Button 
            icon="pi pi-pencil" 
            class="p-button-text p-button-sm mr-1 text-slate-500 hover:text-amber-600" 
            @click="handleEditUser(data)" 
          />
          <Button 
            icon="pi pi-trash" 
            class="p-button-text p-button-sm text-red-500 hover:text-red-600" 
            @click="handleDeleteUser(data.id)" 
          />
        </template>
      </Column>
    </DataTable>

    <!-- Add User Dialog -->
    <Dialog 
      v-model:visible="showAddDialog" 
      header="Create Admin User" 
      :modal="true" 
      class="w-full max-w-[calc(100vw-2rem)] md:max-w-md bg-white border border-slate-200 rounded-xl overflow-hidden shadow-2xl"
      contentClass="p-6 space-y-4"
    >
      <div class="flex flex-col gap-2">
        <label for="username" class="text-sm font-semibold text-slate-700">Username</label>
        <InputText 
          id="username" 
          v-model="newUsername" 
          placeholder="Username for login" 
          class="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
          @keyup.enter="handleAddUser"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label for="password" class="text-sm font-semibold text-slate-700">Password</label>
        <InputText 
          id="password" 
          type="password"
          v-model="newPassword" 
          placeholder="Password" 
          class="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
          @keyup.enter="handleAddUser"
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
            label="Save User" 
            icon="pi pi-check" 
            :loading="submitting"
            class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-semibold py-2 px-4 rounded-lg" 
            @click="handleAddUser" 
          />
        </div>
      </template>
    </Dialog>

    <!-- Edit Password Dialog -->
    <Dialog 
      v-model:visible="showEditDialog" 
      header="Change Password" 
      :modal="true" 
      class="w-full max-w-[calc(100vw-2rem)] md:max-w-md bg-white border border-slate-200 rounded-xl overflow-hidden shadow-2xl"
      contentClass="p-6 space-y-4"
    >
      <p class="text-sm text-slate-500" v-if="editingUser">
        Changing password for <strong class="text-slate-700">{{ editingUser.username }}</strong>
      </p>

      <div class="flex flex-col gap-2">
        <label for="new-password" class="text-sm font-semibold text-slate-700">New Password</label>
        <InputText 
          id="new-password" 
          type="password"
          v-model="editPassword" 
          placeholder="New password" 
          class="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
          @keyup.enter="handleUpdatePassword"
        />
      </div>

      <template #footer>
        <div class="flex justify-end gap-3 mt-4 border-t border-slate-100 pt-4">
          <Button 
            label="Cancel" 
            class="p-button-text text-slate-500 font-medium py-2 px-4 hover:bg-slate-100 rounded-lg" 
            @click="showEditDialog = false" 
          />
          <Button 
            label="Update Password" 
            icon="pi pi-check" 
            :loading="submitting"
            class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-semibold py-2 px-4 rounded-lg" 
            @click="handleUpdatePassword" 
          />
        </div>
      </template>
    </Dialog>
  </div>
</template>
