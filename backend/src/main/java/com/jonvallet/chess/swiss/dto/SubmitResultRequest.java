package com.jonvallet.chess.swiss.dto;

import com.jonvallet.chess.swiss.model.MatchResult;

public class SubmitResultRequest {
    private MatchResult result;

    public SubmitResultRequest() {
    }

    public MatchResult getResult() { return result; }
    public void setResult(MatchResult result) { this.result = result; }
}
