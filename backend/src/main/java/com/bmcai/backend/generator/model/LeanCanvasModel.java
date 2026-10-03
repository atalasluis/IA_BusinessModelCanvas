package com.bmcai.backend.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeanCanvasModel(
    List<String> problemas,
    List<String> alternativas_existentes,
    List<String> solucion,
    List<String> metricas_clave,
    List<String> propuesta_valor_unica,
    List<String> concepto_alto_nivel,
    List<String> ventaja_injusta,
    List<String> canales,
    List<String> segmentos_clientes,
    List<String> early_adopters,
    List<String> estructura_costos,
    List<String> fuentes_ingresos
) {}
