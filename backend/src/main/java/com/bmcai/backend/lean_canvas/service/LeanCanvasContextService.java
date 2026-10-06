package com.bmcai.backend.lean_canvas.service;

import com.bmcai.backend.embedding.model.SearchResult;
import com.bmcai.backend.embedding.service.LeanCanvasSemanticSearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class LeanCanvasContextService {

    private static final int SEARCH_TOP_K = 8;
    private static final int GENERAL_SEARCH_TOP_K = 15;
    private static final int MAX_GENERAL_SOURCES = 3;

    private final LeanCanvasSemanticSearchService searchService;

    public LeanCanvasContextService(
            LeanCanvasSemanticSearchService searchService
    ) {
        this.searchService = searchService;
    }

    public String buildPrompt(
            String context,
            String parameters
    ) {

        if (context == null || context.isBlank()) {
            throw new IllegalArgumentException(
                    "El contexto del proyecto no puede estar vacío"
            );
        }

        if (parameters == null) {
            parameters = "";
        }

        String normalizedParameters = parameters.trim();

        List<SearchResult> selectedResults = new ArrayList<>();

        /*
         * Consultas específicas para cada bloque del Lean Canvas.
         */
        List<BlockQuery> blockQueries = List.of(

                new BlockQuery(
                        "problema.md",
                        "Problemas principales que enfrenta el segmento de clientes, "
                                + "necesidades, frustraciones, dificultades, resultados no deseados "
                                + "y alternativas actuales para resolver esos problemas."
                ),

                new BlockQuery(
                        "segmentos-clientes.md",
                        "Segmentos de clientes, usuarios, compradores, early adopters, "
                                + "características del cliente objetivo y segmentación específica."
                ),

                new BlockQuery(
                        "propuesta-valor.md",
                        "Propuesta única de valor, beneficio principal para el cliente, "
                                + "diferenciación, problema que resuelve y valor frente a alternativas."
                ),

                new BlockQuery(
                        "solucion.md",
                        "Solución mínima para resolver los problemas principales, "
                                + "producto mínimo viable, funcionalidades esenciales y relación "
                                + "entre problema y solución."
                ),

                new BlockQuery(
                        "canales.md",
                        "Canales para llegar a los segmentos de clientes, adquisición, "
                                + "comunicación, distribución, canales digitales, directos, "
                                + "inbound y outbound."
                ),

                new BlockQuery(
                        "flujos-ingresos.md",
                        "Modelo de ingresos, precios, disposición a pagar, formas de monetización, "
                                + "cobros y relación entre ingresos, clientes y propuesta de valor."
                ),

                new BlockQuery(
                        "estructura-costes.md",
                        "Estructura de costes, costes iniciales, costes fijos, costes variables, "
                                + "operación, desarrollo, adquisición de clientes y mantenimiento."
                ),

                new BlockQuery(
                        "metricas-clave.md",
                        "Métricas clave accionables, adquisición, activación, retención, ingresos, "
                                + "uso, conversión, crecimiento y validación del modelo."
                ),

                new BlockQuery(
                        "ventaja-injusta.md",
                        "Ventaja injusta, activos difíciles de copiar o comprar, barreras "
                                + "competitivas, efecto de red, reputación y ventajas sostenibles."
                )
        );

        /*
         * Recuperación específica por bloque.
         * Solo se conserva el mejor fragmento correspondiente
         * al documento objetivo.
         */
        for (BlockQuery block : blockQueries) {

            String query =
                    block.description()
                            + "\n\nContexto del proyecto:\n"
                            + context
                            + "\n\nParámetros del proyecto:\n"
                            + normalizedParameters;

            List<SearchResult> results =
                    searchService.search(
                            query,
                            SEARCH_TOP_K
                    );

            SearchResult bestResult =
                    results.stream()
                            .filter(result ->
                                    result.getChunk()
                                            .getDocumentName()
                                            .equalsIgnoreCase(
                                                    block.documentName()
                                            )
                            )
                            .max(
                                    Comparator.comparingDouble(
                                            SearchResult::getSimilarity
                                    )
                            )
                            .orElse(null);

            if (bestResult != null) {
                selectedResults.add(bestResult);
            }
        }

        /*
         * Recuperación de la introducción metodológica.
         */
        String introductionQuery =
                "Principios generales de Lean Canvas, Running Lean, "
                        + "documentar el plan, identificar riesgos, formular hipótesis, "
                        + "validar el modelo de negocio y construir un Lean Canvas.\n\n"
                        + "Contexto del proyecto:\n"
                        + context
                        + "\n\nParámetros del proyecto:\n"
                        + normalizedParameters;

        List<SearchResult> introductionResults =
                searchService.search(
                        introductionQuery,
                        SEARCH_TOP_K
                );

        SearchResult bestIntroduction =
                introductionResults.stream()
                        .filter(result ->
                                result.getChunk()
                                        .getDocumentName()
                                        .equalsIgnoreCase(
                                                "introduccion.md"
                                        )
                        )
                        .max(
                                Comparator.comparingDouble(
                                        SearchResult::getSimilarity
                                )
                        )
                        .orElse(null);

        if (bestIntroduction != null) {
            selectedResults.add(bestIntroduction);
        }

        /*
         * Búsqueda general para recuperar información adicional
         * relevante que no haya sido encontrada por los bloques.
         */
        String generalQuery =
                context
                        + "\n"
                        + normalizedParameters
                        + "\nLean Canvas modelo de negocio";

        List<SearchResult> generalResults =
                searchService.search(
                        generalQuery,
                        GENERAL_SEARCH_TOP_K
                );

        Set<String> existingSources = new LinkedHashSet<>();

        for (SearchResult result : selectedResults) {
            existingSources.add(
                    result.getChunk()
                            .getDocumentName()
            );
        }

        int additionalSources = 0;

        for (SearchResult result : generalResults) {

            String documentName =
                    result.getChunk()
                            .getDocumentName();

            if (existingSources.contains(documentName)) {
                continue;
            }

            selectedResults.add(result);
            existingSources.add(documentName);

            additionalSources++;

            if (additionalSources >= MAX_GENERAL_SOURCES) {
                break;
            }
        }

        /*
         * Orden de las fuentes:
         * 1. Los 9 bloques.
         * 2. Introducción.
         * 3. Fuentes adicionales.
         */
        selectedResults.sort(
                Comparator.comparingInt(
                        (SearchResult result) ->
                                blockPriority(
                                        result.getChunk()
                                                .getDocumentName()
                                )
                ).thenComparing(
                        Comparator.comparingDouble(
                                SearchResult::getSimilarity
                        ).reversed()
                )
        );

        StringBuilder knowledgeContext =
                new StringBuilder();

        for (SearchResult result : selectedResults) {

            knowledgeContext
                    .append("[Fuente: ")
                    .append(
                            result.getChunk()
                                    .getDocumentName()
                    )
                    .append("]\n");

            knowledgeContext
                    .append("[Similitud: ")
                    .append(
                            String.format(
                                    "%.4f",
                                    result.getSimilarity()
                            )
                    )
                    .append("]\n");

            knowledgeContext
                    .append(
                            result.getChunk()
                                    .getContent()
                    )
                    .append("\n\n");
        }

        return buildPrompt(
                context,
                normalizedParameters,
                knowledgeContext.toString()
        );
    }

    private String buildPrompt(
            String context,
            String parameters,
            String knowledgeContext
    ) {

        return """
                Eres un asistente especializado en Lean Canvas
                y modelos de negocio.

                Tu tarea es construir un Lean Canvas coherente,
                específico y fundamentado para el proyecto descrito
                por el usuario.

                CONTEXTO DEL PROYECTO:
                %s

                PARÁMETROS DEL PROYECTO:
                %s

                CONOCIMIENTO DE REFERENCIA:
                %s

                INSTRUCCIONES:

                1. Genera un Lean Canvas completo utilizando
                   exactamente estos 9 bloques:

                   - Problema
                   - Segmentos de clientes
                   - Propuesta de valor única
                   - Solución
                   - Canales
                   - Flujos de ingresos
                   - Estructura de costes
                   - Métricas clave
                   - Ventaja injusta

                2. Utiliza el conocimiento de referencia como
                   orientación metodológica.

                3. Adapta todo el contenido al contexto y
                   parámetros proporcionados por el usuario.

                4. No copies literalmente los documentos de
                   conocimiento. Utiliza sus conceptos para
                   generar contenido específico para el proyecto.

                5. Evita repetir información entre bloques.

                6. El bloque Problema debe identificar problemas
                   concretos del segmento objetivo y no describir
                   soluciones o funcionalidades.

                7. Los Segmentos de clientes deben ser específicos.
                   Cuando sea posible, diferencia entre:

                   - usuario;
                   - cliente;
                   - comprador;
                   - early adopter;
                   - segmento secundario.

                8. La Propuesta de valor única debe explicar:

                   - para quién es;
                   - qué problema resuelve;
                   - qué beneficio ofrece;
                   - por qué es relevante frente a alternativas.

                9. Diferencia claramente la Propuesta de valor
                   de la Solución.

                10. La Solución debe estar relacionada directamente
                    con los problemas identificados y representar
                    una solución mínima y realista.

                11. Los Canales deben ser coherentes con los
                    segmentos de clientes.

                12. Los Flujos de ingresos deben ser coherentes
                    con el producto, servicio, clientes y mercado.

                13. La Estructura de costes debe identificar los
                    principales costes necesarios para desarrollar,
                    operar y comercializar el proyecto.

                14. Las Métricas clave deben ser accionables y
                    permitir evaluar adquisición, activación,
                    retención, ingresos, uso o validación.

                15. La Ventaja injusta debe representar un elemento
                    difícil de copiar o comprar.

                    Si todavía no existe una ventaja injusta
                    demostrable, indícalo claramente y no inventes
                    una.

                DATOS NUMÉRICOS Y FUENTES:

                16. Cuando exista información externa verificable
                    relacionada con el proyecto, utilízala para
                    enriquecer el Lean Canvas.

                17. Da prioridad a información cuantitativa cuando
                    sea relevante, por ejemplo:

                    - tamaño del mercado;
                    - cantidad de usuarios;
                    - cantidad de clientes;
                    - porcentajes;
                    - precios;
                    - rangos de precios;
                    - costes;
                    - crecimiento;
                    - frecuencia de uso;
                    - tasas de conversión;
                    - estadísticas;
                    - otros indicadores relevantes.

                18. Los datos numéricos deben aparecer dentro del
                    bloque del Lean Canvas al que correspondan.

                    Ejemplos:

                    - tamaño del mercado → Segmentos de clientes;
                    - precios → Flujos de ingresos;
                    - costes → Estructura de costes;
                    - tasas de conversión o retención → Métricas clave;
                    - estadísticas sobre un problema → Problema.

                19. NO inventes datos numéricos.

                20. No presentes estimaciones, suposiciones o
                    aproximaciones como datos reales.

                21. Cuando utilices un dato externo verificable,
                    conserva la relación entre el dato y su fuente.

                22. Para cada dato externo utilizado, proporciona
                    en la sección Fuentes y bibliografía:

                    - nombre de la fuente;
                    - organización o autor, si está disponible;
                    - año, si está disponible;
                    - URL verificable.

                23. No inventes URLs, nombres de fuentes, autores
                    ni fechas.

                24. Si no se dispone de información externa
                    verificable para respaldar datos numéricos,
                    no inventes cifras.

                25. Si se encontró información externa verificable,
                    inclúyela en el bloque correspondiente y agrega
                    su fuente y URL en Fuentes y bibliografía.

                26. Si no se encontró información externa
                    verificable, NO escribas esa aclaración dentro
                    de los 9 bloques.

                    Indica únicamente en Fuentes y bibliografía:

                    "No se encontró información externa verificable
                    para respaldar datos numéricos adicionales."

                27. La información de los documentos de conocimiento
                    proporcionados en este contexto es orientación
                    metodológica. No debe convertirse automáticamente
                    en estadísticas reales del mercado.

                FUENTES Y BIBLIOGRAFÍA:

                28. Después de los 9 bloques agrega únicamente
                    una sección llamada:

                    FUENTES Y BIBLIOGRAFÍA

                29. Incluye aquí las fuentes utilizadas para
                    respaldar datos externos o afirmaciones
                    verificables.

                30. Cada fuente debe incluir, cuando esté disponible:

                    - título o nombre;
                    - organización o autor;
                    - año;
                    - URL.

                31. Las URLs deben aparecer únicamente en
                    Fuentes y bibliografía.

                32. Si no se encontró información externa
                    verificable, utiliza:

                    No se encontró información externa verificable
                    para respaldar datos numéricos adicionales.

                RESTRICCIONES DE RESPUESTA:

                33. La respuesta debe contener únicamente:

                    1. Los 9 bloques del Lean Canvas.
                    2. Fuentes y bibliografía.

                34. NO agregues:

                    - Datos y supuestos;
                    - Prototipo;
                    - Mockup;
                    - explicación metodológica;
                    - explicación del proceso;
                    - comentarios fuera del Lean Canvas;
                    - un décimo bloque.

                35. Utiliza exactamente esta estructura:

                LEAN CANVAS

                Problema:
                - ...

                Segmentos de clientes:
                - ...

                Propuesta de valor única:
                - ...

                Solución:
                - ...

                Canales:
                - ...

                Flujos de ingresos:
                - ...

                Estructura de costes:
                - ...

                Métricas clave:
                - ...

                Ventaja injusta:
                - ...

                FUENTES Y BIBLIOGRAFÍA

                - Fuente: ...
                  Organización/Autor: ...
                  Año: ...
                  URL: ...

                36. Si un dato numérico externo se utiliza dentro
                    de un bloque, debe existir una fuente
                    correspondiente en Fuentes y bibliografía.

                37. Si no existe información suficiente para
                    respaldar un dato numérico, no lo inventes.

                38. Prioriza información específica y útil para
                    el proyecto sobre explicaciones generales
                    de Lean Canvas.

                39. No agregues información que contradiga el
                    contexto o los parámetros proporcionados.

                40. Si existe incertidumbre sobre un dato, no lo
                    presentes como un hecho verificable.
                """.formatted(
                context,
                parameters,
                knowledgeContext
        );
    }

    private int blockPriority(String documentName) {

        return switch (documentName.toLowerCase()) {

            case "problema.md" -> 1;
            case "segmentos-clientes.md" -> 2;
            case "propuesta-valor.md" -> 3;
            case "solucion.md" -> 4;
            case "canales.md" -> 5;
            case "flujos-ingresos.md" -> 6;
            case "estructura-costes.md" -> 7;
            case "metricas-clave.md" -> 8;
            case "ventaja-injusta.md" -> 9;
            case "introduccion.md" -> 10;

            default -> 100;
        };
    }

    private record BlockQuery(
            String documentName,
            String description
    ) {
    }
}
