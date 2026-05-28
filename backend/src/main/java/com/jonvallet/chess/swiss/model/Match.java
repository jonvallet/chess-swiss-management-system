package com.jonvallet.chess.swiss.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @ManyToOne
    @JoinColumn(name = "white_player_id")
    private Player whitePlayer;

    @ManyToOne
    @JoinColumn(name = "black_player_id")
    private Player blackPlayer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchResult result = MatchResult.UNPLAYED;

    @Column(name = "is_bye", nullable = false)
    private Boolean isBye = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Match() {
    }

    public Match(UUID id, Tournament tournament, Integer roundNumber, Player whitePlayer, Player blackPlayer, MatchResult result, Boolean isBye, LocalDateTime createdAt) {
        this.id = id;
        this.tournament = tournament;
        this.roundNumber = roundNumber;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.result = result != null ? result : MatchResult.UNPLAYED;
        this.isBye = isBye != null ? isBye : false;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static class Builder {
        private UUID id;
        private Tournament tournament;
        private Integer roundNumber;
        private Player whitePlayer;
        private Player blackPlayer;
        private MatchResult result = MatchResult.UNPLAYED;
        private Boolean isBye = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder tournament(Tournament tournament) { this.tournament = tournament; return this; }
        public Builder roundNumber(Integer roundNumber) { this.roundNumber = roundNumber; return this; }
        public Builder whitePlayer(Player whitePlayer) { this.whitePlayer = whitePlayer; return this; }
        public Builder blackPlayer(Player blackPlayer) { this.blackPlayer = blackPlayer; return this; }
        public Builder result(MatchResult result) { this.result = result; return this; }
        public Builder isBye(Boolean isBye) { this.isBye = isBye; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Match build() {
            return new Match(id, tournament, roundNumber, whitePlayer, blackPlayer, result, isBye, createdAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public Player getWhitePlayer() { return whitePlayer; }
    public void setWhitePlayer(Player whitePlayer) { this.whitePlayer = whitePlayer; }

    public Player getBlackPlayer() { return blackPlayer; }
    public void setBlackPlayer(Player blackPlayer) { this.blackPlayer = blackPlayer; }

    public MatchResult getResult() { return result; }
    public void setResult(MatchResult result) { this.result = result; }

    public Boolean getIsBye() { return isBye; }
    public void setIsBye(Boolean isBye) { this.isBye = isBye; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
