package com.jonvallet.chess.swiss.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_rounds", nullable = false)
    private Integer totalRounds;

    @Column(name = "current_round", nullable = false)
    private Integer currentRound = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TournamentStatus status = TournamentStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Tournament() {
    }

    public Tournament(UUID id, String name, Integer totalRounds, Integer currentRound, TournamentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.totalRounds = totalRounds;
        this.currentRound = currentRound;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static class Builder {
        private UUID id;
        private String name;
        private Integer totalRounds;
        private Integer currentRound = 0;
        private TournamentStatus status = TournamentStatus.DRAFT;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder totalRounds(Integer totalRounds) { this.totalRounds = totalRounds; return this; }
        public Builder currentRound(Integer currentRound) { this.currentRound = currentRound; return this; }
        public Builder status(TournamentStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Tournament build() {
            return new Tournament(id, name, totalRounds, currentRound, status, createdAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getTotalRounds() { return totalRounds; }
    public void setTotalRounds(Integer totalRounds) { this.totalRounds = totalRounds; }

    public Integer getCurrentRound() { return currentRound; }
    public void setCurrentRound(Integer currentRound) { this.currentRound = currentRound; }

    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
