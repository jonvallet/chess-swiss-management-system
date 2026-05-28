package com.jonvallet.chess.swiss.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TournamentPlayerId implements Serializable {
    private UUID tournamentId;
    private UUID playerId;

    public TournamentPlayerId() {
    }

    public TournamentPlayerId(UUID tournamentId, UUID playerId) {
        this.tournamentId = tournamentId;
        this.playerId = playerId;
    }

    // Getters and Setters
    public UUID getTournamentId() { return tournamentId; }
    public void setTournamentId(UUID tournamentId) { this.tournamentId = tournamentId; }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentPlayerId that = (TournamentPlayerId) o;
        return Objects.equals(tournamentId, that.tournamentId) &&
                Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tournamentId, playerId);
    }
}
