package com.bmcai.backend.generator.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CanvasModel(
    List<String> alianzas_clave,
    List<String> actividades_clave,
    List<String> recursos_clave,
    
    @JsonAlias({"propuesta_valor", "propuestas_valor"})
    List<String> propuesta_valor,
    
    List<String> relacion_clientes,
    List<String> canales,
    List<String> segmentos_clientes,
    List<String> estructura_costos,
    List<String> fuentes_ingresos
) {}
