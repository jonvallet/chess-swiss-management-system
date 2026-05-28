package com.jonvallet.chess.swiss.dto;

public class CreatePlayerRequest {
    private String name;
    private Integer rating = 1200;

    public CreatePlayerRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
