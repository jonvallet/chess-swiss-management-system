package com.jonvallet.chess.swiss.dto;

import java.util.UUID;

public class JoinTournamentResponse {
    private String token;
    private UUID playerId;
    private UUID tournamentId;

    public JoinTournamentResponse() {}

    public JoinTournamentResponse(String token, UUID playerId, UUID tournamentId) {
        this.token = token;
        this.playerId = playerId;
        this.tournamentId = tournamentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(UUID tournamentId) {
        this.tournamentId = tournamentId;
    }
}
