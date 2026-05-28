package com.jonvallet.chess.swiss.dto;

import java.util.UUID;

public class RegisterPlayerRequest {
    private UUID playerId;

    public RegisterPlayerRequest() {
    }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
}
