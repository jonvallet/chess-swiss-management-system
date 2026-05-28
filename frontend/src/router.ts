import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from './views/Dashboard.vue'
import Players from './views/Players.vue'
import TournamentDetail from './views/TournamentDetail.vue'
import JoinTournament from './views/JoinTournament.vue'
import Login from './views/Login.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/players',
    name: 'Players',
    component: Players,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/tournaments/:id',
    name: 'TournamentDetail',
    component: TournamentDetail,
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/join/:code',
    name: 'JoinTournament',
    component: JoinTournament,
    props: true,
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('auth_token')
  const role = localStorage.getItem('auth_role')

  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    return next('/login')
  }

  if (to.path === '/login' && token && role === 'ADMIN') {
    return next('/')
  }

  next()
})

export default router
