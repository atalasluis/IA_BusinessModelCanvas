package com.bmcai.backend.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateResponse(
    @JsonAlias({"jtbd_analysis", "metodologia_jtbd", "jobs_to_be_done"})
    JtbdAnalysis jtbd_analysis,

    @JsonAlias({"business_model_canvas", "canvas"})
    CanvasModel business_model_canvas
) {}
