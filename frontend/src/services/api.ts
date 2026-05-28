import axios from 'axios'

// Set up the default axios instance
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

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

// API methods
export const PlayerService = {
  getAll: () => api.get<Player[]>('/players').then(r => r.data),
  create: (name: string, rating: number) => 
    api.post<Player>('/players', { name, rating }).then(r => r.data),
  delete: (id: string) => api.delete(`/players/${id}`).then(r => r.data)
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
