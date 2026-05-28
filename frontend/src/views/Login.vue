<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '../services/api'
import { useAuthStore } from '../stores/auth'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (!username.value.trim() || !password.value) {
    errorMsg.value = 'Please enter both username and password.'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const response = await AuthService.login(username.value.trim(), password.value)
    authStore.setAuth(response.token, response.role)
    router.push('/')
  } catch (err) {
    console.error('Login failed:', err)
    errorMsg.value = 'Invalid username or password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center p-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-lg border border-slate-200 overflow-hidden">
      <div class="bg-slate-950 text-white p-8 text-center">
        <div class="w-16 h-16 bg-amber-500 rounded-full flex items-center justify-center mx-auto mb-4">
          <i class="pi pi-trophy text-3xl text-slate-950"></i>
        </div>
        <h1 class="text-2xl font-bold tracking-tight">SwissChess</h1>
        <p class="text-sm text-slate-400 mt-1">Tournament Management System</p>
      </div>

      <div class="p-8 space-y-6">
        <div class="text-center">
          <h2 class="text-lg font-bold text-slate-800">Admin Login</h2>
          <p class="text-xs text-slate-500 mt-1">Enter your credentials to access the dashboard.</p>
        </div>

        <div class="space-y-4">
          <div class="flex flex-col gap-1.5">
            <label for="username" class="text-xs font-bold text-slate-600 uppercase tracking-wider">Username</label>
            <InputText
              id="username"
              v-model="username"
              placeholder="Enter username"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="flex flex-col gap-1.5">
            <label for="password" class="text-xs font-bold text-slate-600 uppercase tracking-wider">Password</label>
            <InputText
              id="password"
              v-model="password"
              type="password"
              placeholder="Enter password"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-lg text-slate-800 focus:ring-2 focus:ring-amber-400 outline-none"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <div v-if="errorMsg" class="bg-rose-50 text-rose-600 p-3 rounded-lg border border-rose-100 text-xs font-semibold">
          <i class="pi pi-exclamation-circle mr-1"></i> {{ errorMsg }}
        </div>

        <Button
          label="Sign In"
          icon="pi pi-sign-in"
          :loading="loading"
          class="bg-amber-500 hover:bg-amber-600 border-none text-slate-950 font-bold px-5 py-3 rounded-lg text-sm w-full shadow-md"
          @click="handleLogin"
        />

        <div class="text-center pt-4 border-t border-slate-100">
          <p class="text-xs text-slate-400">
            Joining a tournament? Use the share link provided by the organizer.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
