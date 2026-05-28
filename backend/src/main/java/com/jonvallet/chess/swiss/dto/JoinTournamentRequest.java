package com.jonvallet.chess.swiss.dto;

public class JoinTournamentRequest {
    private String playerName;
    private Integer playerRating;

    public JoinTournamentRequest() {}

    public JoinTournamentRequest(String playerName, Integer playerRating) {
        this.playerName = playerName;
        this.playerRating = playerRating;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getPlayerRating() {
        return playerRating;
    }

    public void setPlayerRating(Integer playerRating) {
        this.playerRating = playerRating;
    }
}
