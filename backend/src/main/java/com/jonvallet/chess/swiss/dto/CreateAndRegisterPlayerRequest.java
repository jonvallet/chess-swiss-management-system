package com.jonvallet.chess.swiss.dto;

public class CreateAndRegisterPlayerRequest {
    private String name;
    private Integer rating;

    public CreateAndRegisterPlayerRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
