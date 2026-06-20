<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { name: 'Dashboard', path: '/', icon: 'pi pi-home' },
  { name: 'Players Directory', path: '/players', icon: 'pi pi-users' },
  { name: 'Admin Users', path: '/users', icon: 'pi pi-shield' }
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const showSidebar = route.path !== '/login' && !route.path.startsWith('/join/')

const isSidebarOpen = ref(false)
const sidebarClasses = computed(() =>
  isSidebarOpen.value ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
)

watch(() => route.path, () => {
  isSidebarOpen.value = false
})

watch(isSidebarOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})
</script>

<template>
  <div class="flex h-screen bg-slate-100 overflow-hidden font-sans">
    <div
      v-if="isSidebarOpen"
      class="fixed inset-0 bg-black/50 z-40 md:hidden transition-opacity"
      @click="isSidebarOpen = false"
    ></div>

    <aside v-if="showSidebar && authStore.isAdmin" class="w-64 bg-slate-900 text-white flex flex-col justify-between shrink-0 shadow-lg fixed inset-y-0 left-0 z-50 transition-transform duration-300 ease-in-out md:static md:z-auto" :class="sidebarClasses">
      <div>
        <div class="h-16 flex items-center gap-3 px-6 bg-slate-950 border-b border-slate-800">
          <i class="pi pi-trophy text-amber-500 text-2xl"></i>
          <span class="font-bold text-lg tracking-wider text-slate-100 uppercase">SwissChess</span>
        </div>

        <nav class="p-4 space-y-1">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-150"
            :class="isActive(item.path) 
              ? 'bg-amber-500 text-slate-950 shadow-md font-semibold' 
              : 'text-slate-400 hover:bg-slate-800 hover:text-white'"
            @click="isSidebarOpen = false"
          >
            <i :class="item.icon" class="text-lg"></i>
            {{ item.name }}
          </router-link>
        </nav>
      </div>

      <div class="p-4 bg-slate-950/40 border-t border-slate-800 space-y-3">
        <button
          @click="handleLogout"
          class="w-full flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium text-slate-400 hover:bg-slate-800 hover:text-white transition-all"
        >
          <i class="pi pi-sign-out"></i>
          Sign Out
        </button>
        <div class="text-center text-xs text-slate-500">
          Swiss System Manager v1.0.0
        </div>
      </div>
    </aside>

    <aside v-else-if="showSidebar && authStore.isPlayer" class="w-64 bg-slate-900 text-white flex flex-col justify-between shrink-0 shadow-lg fixed inset-y-0 left-0 z-50 transition-transform duration-300 ease-in-out md:static md:z-auto" :class="sidebarClasses">
      <div>
        <div class="h-16 flex items-center gap-3 px-6 bg-slate-950 border-b border-slate-800">
          <i class="pi pi-trophy text-amber-500 text-2xl"></i>
          <span class="font-bold text-lg tracking-wider text-slate-100 uppercase">SwissChess</span>
        </div>

        <nav class="p-4 space-y-1">
          <router-link
            :to="`/tournaments/${authStore.tournamentId}`"
            class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-150 bg-amber-500 text-slate-950 shadow-md font-semibold"
            @click="isSidebarOpen = false"
          >
            <i class="pi pi-trophy text-lg"></i>
            My Tournament
          </router-link>
        </nav>
      </div>

      <div class="p-4 bg-slate-950/40 border-t border-slate-800 space-y-3">
        <button
          @click="handleLogout"
          class="w-full flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium text-slate-400 hover:bg-slate-800 hover:text-white transition-all"
        >
          <i class="pi pi-sign-out"></i>
          Sign Out
        </button>
        <div class="text-center text-xs text-slate-500">
          Swiss System Manager v1.0.0
        </div>
      </div>
    </aside>

    <div class="flex flex-col flex-1 overflow-hidden">
      <header v-if="showSidebar" class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 md:px-8 shadow-sm shrink-0">
        <div class="flex items-center gap-3">
          <button
            class="md:hidden p-2 rounded-lg text-slate-500 hover:bg-slate-100 hover:text-slate-700 transition-colors"
            @click="isSidebarOpen = !isSidebarOpen"
          >
            <i class="pi pi-bars text-xl"></i>
          </button>
          <h1 class="text-xl font-bold text-slate-800">
            <template v-if="route.path === '/'">Tournaments Dashboard</template>
            <template v-else-if="route.path === '/players'">Players Directory</template>
            <template v-else-if="route.path === '/users'">Admin Users</template>
            <template v-else>Tournament Details</template>
          </h1>
        </div>
        <div class="flex items-center gap-4 text-slate-600">
          <span v-if="authStore.isAdmin" class="text-sm bg-amber-100 text-amber-800 px-3 py-1.5 rounded-full font-medium flex items-center gap-2 border border-amber-200">
            <i class="pi pi-shield text-xs"></i>
            Admin
          </span>
          <span v-else-if="authStore.isPlayer" class="text-sm bg-green-100 text-green-800 px-3 py-1.5 rounded-full font-medium flex items-center gap-2 border border-green-200">
            <i class="pi pi-user text-xs"></i>
            Player
          </span>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-4 md:p-8">
        <div class="max-w-7xl mx-auto h-full">
          <router-view v-slot="{ Component }">
            <transition name="fade">
              <component :is="Component" :key="$route.fullPath" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
