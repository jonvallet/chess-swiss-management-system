import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from './views/Dashboard.vue'
import Players from './views/Players.vue'
import TournamentDetail from './views/TournamentDetail.vue'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/players',
    name: 'Players',
    component: Players
  },
  {
    path: '/tournaments/:id',
    name: 'TournamentDetail',
    component: TournamentDetail,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
