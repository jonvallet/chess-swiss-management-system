package com.jonvallet.chess.swiss.dto;

import java.util.UUID;

public class ViewTournamentResponse {
    private String token;
    private UUID tournamentId;

    public ViewTournamentResponse() {}

    public ViewTournamentResponse(String token, UUID tournamentId) {
        this.token = token;
        this.tournamentId = tournamentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(UUID tournamentId) {
        this.tournamentId = tournamentId;
    }
}
