package com.bmcai.backend.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateLeanResponse(
    LeanCanvasModel lean_canvas
) {}
