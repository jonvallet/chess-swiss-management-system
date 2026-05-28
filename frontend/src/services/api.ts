import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_role')
      localStorage.removeItem('auth_player_id')
      localStorage.removeItem('auth_tournament_id')
      if (window.location.pathname !== '/login' && !window.location.pathname.startsWith('/join/')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Types & Interfaces
export interface Player {
  id: string
  name: string
  rating: number
  createdAt: string
}

export interface Tournament {
  id: string
  name: string
  totalRounds: number
  currentRound: number
  status: 'DRAFT' | 'IN_PROGRESS' | 'FINISHED'
  createdAt: string
  shareCode?: string
}

export interface TournamentPlayer {
  id: {
    tournamentId: string
    playerId: string
  }
  player: Player
  score: number
  colorDifference: number
}

export interface Match {
  id: string
  roundNumber: number
  whitePlayer: Player | null
  blackPlayer: Player | null
  result: 'WHITE_WIN' | 'BLACK_WIN' | 'DRAW' | 'UNPLAYED'
  isBye: boolean
}

export interface PlayerStanding {
  rank: number
  playerId: string
  playerName: string
  rating: number
  score: number
  buchholz: number
  colorDifference: number
}

export interface LoginResponse {
  token: string
  role: string
}

export interface JoinTournamentResponse {
  token: string
  playerId: string
  tournamentId: string
}

// API methods
export const PlayerService = {
  getAll: () => api.get<Player[]>('/players').then(r => r.data),
  create: (name: string, rating: number) => 
    api.post<Player>('/players', { name, rating }).then(r => r.data),
  delete: (id: string) => api.delete(`/players/${id}`).then(r => r.data)
}

export const AuthService = {
  login: (username: string, password: string) =>
    api.post<LoginResponse>('/auth/login', { username, password }).then(r => r.data),
  joinTournament: (tournamentId: string, playerName: string, playerRating: number) =>
    api.post<JoinTournamentResponse>(`/tournaments/${tournamentId}/join`, { playerName, playerRating }).then(r => r.data)
}

export const TournamentService = {
  getAll: () => api.get<Tournament[]>('/tournaments').then(r => r.data),
  getById: (id: string) => api.get<Tournament>(`/tournaments/${id}`).then(r => r.data),
  getByShareCode: (code: string) => 
    api.get<Tournament>(`/tournaments/share/${code}`).then(r => r.data),
  create: (name: string, totalRounds: number) => 
    api.post<Tournament>('/tournaments', { name, totalRounds }).then(r => r.data),
  registerPlayer: (tournamentId: string, playerId: string) => 
    api.post<TournamentPlayer>(`/tournaments/${tournamentId}/register`, { playerId }).then(r => r.data),
  getPlayers: (tournamentId: string) => 
    api.get<TournamentPlayer[]>(`/tournaments/${tournamentId}/players`).then(r => r.data),
  generateNextRound: (tournamentId: string) => 
    api.post<Match[]>(`/tournaments/${tournamentId}/rounds/generate`).then(r => r.data),
  getRoundMatches: (tournamentId: string, roundNum: number) => 
    api.get<Match[]>(`/tournaments/${tournamentId}/rounds/${roundNum}/matches`).then(r => r.data),
  getAllMatches: (tournamentId: string) => 
    api.get<Match[]>(`/tournaments/${tournamentId}/matches`).then(r => r.data),
  submitMatchResult: (matchId: string, result: 'WHITE_WIN' | 'BLACK_WIN' | 'DRAW') => 
    api.post<Match>(`/matches/${matchId}/result`, { result }).then(r => r.data),
  getStandings: (tournamentId: string) => 
    api.get<PlayerStanding[]>(`/tournaments/${tournamentId}/standings`).then(r => r.data)
}

export default api
