package com.jonvallet.chess.swiss.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tournament_players")
public class TournamentPlayer {

    @EmbeddedId
    private TournamentPlayerId id;

    @ManyToOne
    @MapsId("tournamentId")
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne
    @MapsId("playerId")
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(precision = 3, scale = 1, nullable = false)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "color_difference", nullable = false)
    private Integer colorDifference = 0;

    public TournamentPlayer() {
    }

    public TournamentPlayer(TournamentPlayerId id, Tournament tournament, Player player, BigDecimal score, Integer colorDifference) {
        this.id = id;
        this.tournament = tournament;
        this.player = player;
        this.score = score != null ? score : BigDecimal.ZERO;
        this.colorDifference = colorDifference != null ? colorDifference : 0;
    }

    public static class Builder {
        private TournamentPlayerId id;
        private Tournament tournament;
        private Player player;
        private BigDecimal score = BigDecimal.ZERO;
        private Integer colorDifference = 0;

        public Builder id(TournamentPlayerId id) { this.id = id; return this; }
        public Builder tournament(Tournament tournament) { this.tournament = tournament; return this; }
        public Builder player(Player player) { this.player = player; return this; }
        public Builder score(BigDecimal score) { this.score = score; return this; }
        public Builder colorDifference(Integer colorDifference) { this.colorDifference = colorDifference; return this; }

        public TournamentPlayer build() {
            return new TournamentPlayer(id, tournament, player, score, colorDifference);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public TournamentPlayerId getId() { return id; }
    public void setId(TournamentPlayerId id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Integer getColorDifference() { return colorDifference; }
    public void setColorDifference(Integer colorDifference) { this.colorDifference = colorDifference; }
}
