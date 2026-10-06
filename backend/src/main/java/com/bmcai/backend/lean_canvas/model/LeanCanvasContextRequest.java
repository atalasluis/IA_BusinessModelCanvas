package com.bmcai.backend.lean_canvas.model;

public class LeanCanvasContextRequest {

    private String context;
    private String parameters;

    public LeanCanvasContextRequest() {
    }

    public LeanCanvasContextRequest(
            String context,
            String parameters
    ) {
        this.context = context;
        this.parameters = parameters;
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