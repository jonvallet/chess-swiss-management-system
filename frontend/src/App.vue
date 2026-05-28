<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { name: 'Dashboard', path: '/', icon: 'pi pi-home' },
  { name: 'Players Directory', path: '/players', icon: 'pi pi-users' }
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="flex h-screen bg-slate-100 overflow-hidden font-sans">
    <!-- Sidebar -->
    <aside class="w-64 bg-slate-900 text-white flex flex-col justify-between shrink-0 shadow-lg">
      <div>
        <!-- Logo Header -->
        <div class="h-16 flex items-center gap-3 px-6 bg-slate-950 border-b border-slate-800">
          <i class="pi pi-trophy text-amber-500 text-2xl"></i>
          <span class="font-bold text-lg tracking-wider text-slate-100 uppercase">SwissChess</span>
        </div>

        <!-- Navigation Links -->
        <nav class="p-4 space-y-1">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-150"
            :class="isActive(item.path) 
              ? 'bg-amber-500 text-slate-950 shadow-md font-semibold' 
              : 'text-slate-400 hover:bg-slate-800 hover:text-white'"
          >
            <i :class="item.icon" class="text-lg"></i>
            {{ item.name }}
          </router-link>
        </nav>
      </div>

      <!-- Footer / Version -->
      <div class="p-4 bg-slate-950/40 border-t border-slate-800 text-center text-xs text-slate-500">
        Swiss System Manager v1.0.0
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="flex flex-col flex-1 overflow-hidden">
      <!-- Top Navbar -->
      <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 shadow-sm shrink-0">
        <div class="flex items-center gap-3">
          <h1 class="text-xl font-bold text-slate-800">
            <template v-if="route.path === '/'">Tournaments Dashboard</template>
            <template v-else-if="route.path === '/players'">Players Directory</template>
            <template v-else>Tournament Details</template>
          </h1>
        </div>
        <div class="flex items-center gap-4 text-slate-600">
          <span class="text-sm bg-slate-100 px-3 py-1.5 rounded-full font-medium flex items-center gap-2 border border-slate-200 shadow-xs">
            <span class="w-2.5 h-2.5 bg-green-500 rounded-full animate-pulse"></span>
            Database: Connected (Supabase)
          </span>
        </div>
      </header>

      <!-- Subview Content Panel -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto h-full">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<style>
/* Transition styling */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
