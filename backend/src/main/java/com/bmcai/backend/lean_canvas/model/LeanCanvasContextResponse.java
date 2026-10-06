package com.bmcai.backend.lean_canvas.model;

public class LeanCanvasContextResponse {

    private String prompt;

    public LeanCanvasContextResponse() {
    }

    public LeanCanvasContextResponse(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}