package com.jonvallet.chess.swiss.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer rating = 1200;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Player() {
    }

    public Player(String name, Integer rating) {
        this.name = name;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }

    public Player(UUID id, String name, Integer rating, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    // Static Builder class for convenience
    public static class Builder {
        private UUID id;
        private String name;
        private Integer rating = 1200;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder rating(Integer rating) { this.rating = rating; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        
        public Player build() {
            return new Player(id, name, rating, createdAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
