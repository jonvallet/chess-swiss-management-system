package com.jonvallet.chess.swiss.dto;

public class UpdateUserRequest {
    private String password;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
