package com.jonvallet.chess.swiss.dto;

public class CreateTournamentRequest {
    private String name;
    private Integer totalRounds;

    public CreateTournamentRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getTotalRounds() { return totalRounds; }
    public void setTotalRounds(Integer totalRounds) { this.totalRounds = totalRounds; }
}
