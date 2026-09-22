package com.bmcai.backend.bmc.model;

public class BmcContextResponse {

    private String prompt;

    public BmcContextResponse() {
    }

    public BmcContextResponse(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}