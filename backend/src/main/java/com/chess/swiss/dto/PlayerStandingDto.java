package com.chess.swiss.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerStandingDto {
    private Integer rank;
    private UUID playerId;
    private String playerName;
    private Integer rating;
    private BigDecimal score;
    private BigDecimal buchholz;
    private Integer colorDifference;

    public PlayerStandingDto() {
    }

    public PlayerStandingDto(Integer rank, UUID playerId, String playerName, Integer rating, BigDecimal score, BigDecimal buchholz, Integer colorDifference) {
        this.rank = rank;
        this.playerId = playerId;
        this.playerName = playerName;
        this.rating = rating;
        this.score = score;
        this.buchholz = buchholz;
        this.colorDifference = colorDifference;
    }

    public static class Builder {
        private Integer rank;
        private UUID playerId;
        private String playerName;
        private Integer rating;
        private BigDecimal score;
        private BigDecimal buchholz;
        private Integer colorDifference;

        public Builder rank(Integer rank) { this.rank = rank; return this; }
        public Builder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public Builder playerName(String playerName) { this.playerName = playerName; return this; }
        public Builder rating(Integer rating) { this.rating = rating; return this; }
        public Builder score(BigDecimal score) { this.score = score; return this; }
        public Builder buchholz(BigDecimal buchholz) { this.buchholz = buchholz; return this; }
        public Builder colorDifference(Integer colorDifference) { this.colorDifference = colorDifference; return this; }

        public PlayerStandingDto build() {
            return new PlayerStandingDto(rank, playerId, playerName, rating, score, buchholz, colorDifference);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public BigDecimal getBuchholz() { return buchholz; }
    public void setBuchholz(BigDecimal buchholz) { this.buchholz = buchholz; }

    public Integer getColorDifference() { return colorDifference; }
    public void setColorDifference(Integer colorDifference) { this.colorDifference = colorDifference; }

    @Override
    public String toString() {
        return "PlayerStandingDto{" +
                "rank=" + rank +
                ", playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", rating=" + rating +
                ", score=" + score +
                ", buchholz=" + buchholz +
                ", colorDifference=" + colorDifference +
                '}';
    }
}
