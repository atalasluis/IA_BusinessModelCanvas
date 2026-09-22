package com.bmcai.backend.bmc.model;

public class BmcContextRequest {

    private String context;
    private String parameters;

    public BmcContextRequest() {
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}