package com.bmcai.backend.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JtbdAnalysis(
    @JsonAlias({"job_principal", "main_job"})
    String job_principal,

    @JsonAlias({"job_funcional", "functional_job"})
    String job_funcional,

    @JsonAlias({"job_social", "social_job"})
    String job_social,

    @JsonAlias({"job_emocional", "emotional_job"})
    String job_emocional,

    List<String> necesidades,
    List<String> dolores,
    List<String> beneficios,
    String resultado_esperado
) {}
