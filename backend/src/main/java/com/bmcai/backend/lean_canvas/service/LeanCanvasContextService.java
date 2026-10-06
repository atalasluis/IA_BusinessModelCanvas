package com.bmcai.backend.lean_canvas.service;

import com.bmcai.backend.embedding.model.SearchResult;
import com.bmcai.backend.embedding.service.LeanCanvasSemanticSearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LeanCanvasContextService {

    private static final int SEARCH_TOP_K = 8;

    private static final int GENERAL_SEARCH_TOP_K = 15;

    private static final int MAX_GENERAL_SOURCES = 3;

    private static final Map<String, String> LEAN_CANVAS_BLOCK_QUERIES =
            Map.ofEntries(

                    Map.entry(
                            "problema.md",
                            "problemas principales necesidades dolores dificultades situaciones problemáticas alternativas actuales y necesidades del cliente"
                    ),

                    Map.entry(
                            "segmentos-clientes.md",
                            "segmentos de clientes usuarios compradores early adopters características necesidades comportamiento contexto y perfiles de clientes"
                    ),

                    Map.entry(
                            "propuesta-valor.md",
                            "propuesta de valor única beneficios diferenciación problema principal resultado deseado valor para el cliente y diferencia frente a alternativas"
                    ),

                    Map.entry(
                            "solucion.md",
                            "solución propuesta características producto servicio funcionalidades solución mínima producto mínimo viable y forma de resolver los problemas"
                    ),

                    Map.entry(
                            "canales.md",
                            "canales de comunicación distribución adquisición marketing ventas alcance medios para llegar a clientes y estrategias de contacto"
                    ),

                    Map.entry(
                            "flujos-ingresos.md",
                            "flujos de ingresos modelo de monetización precios pagos suscripción compra única pago por uso servicios freemium y disposición a pagar"
                    ),

                    Map.entry(
                            "estructura-costes.md",
                            "estructura de costes costos principales gastos costes fijos variables recursos necesarios adquisición operación desarrollo y validación"
                    ),

                    Map.entry(
                            "metricas-clave.md",
                            "métricas clave indicadores KPI adquisición activación retención ingresos recomendación conversión crecimiento y medición del modelo"
                    ),

                    Map.entry(
                            "ventaja-injusta.md",
                            "ventaja injusta diferenciación sostenible barreras competitivas elementos difíciles de copiar comunidad efectos de red información exclusiva posicionamiento y activos estratégicos"
                    )
            );

    private static final String INTRODUCTION_DOCUMENT =
            "introduccion.md";

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

        validate(context, parameters);

        List<SearchResult> selectedResults =
                new ArrayList<>();

        Set<String> usedContent =
                new LinkedHashSet<>();

        /*
         * Recuperación específica de cada bloque.
         *
         * La consulta combina:
         *
         * 1. El objetivo del bloque.
         * 2. El contexto concreto del proyecto.
         * 3. Los parámetros proporcionados por el usuario.
         *
         * De esta forma la búsqueda semántica no recupera
         * solamente información genérica sobre Lean Canvas,
         * sino información metodológica relevante para el
         * proyecto concreto.
         */
        for (Map.Entry<String, String> entry :
                LEAN_CANVAS_BLOCK_QUERIES.entrySet()) {

            String blockQuery =
                    buildBlockQuery(
                            entry.getValue(),
                            context,
                            parameters
                    );

            List<SearchResult> results =
                    searchService.search(
                            blockQuery,
                            SEARCH_TOP_K
                    );

            addBestResultForDocument(
                    selectedResults,
                    usedContent,
                    results,
                    entry.getKey()
            );
        }

        /*
         * Recuperación de la metodología general.
         *
         * Se mantiene separada de los nueve bloques para que
         * introduccion.md no domine el contexto específico.
         */
        String introductionQuery = """
                Lean Canvas metodología general propósito estructura
                nueve bloques hipótesis problema segmento cliente
                propuesta solución canales ingresos costes métricas
                ventaja injusta y validación.

                Proyecto:
                %s

                Parámetros:
                %s
                """.formatted(
                context,
                parameters
        );

        List<SearchResult> introductionResults =
                searchService.search(
                        introductionQuery,
                        SEARCH_TOP_K
                );

        addBestResultForDocument(
                selectedResults,
                usedContent,
                introductionResults,
                INTRODUCTION_DOCUMENT
        );

        /*
         * Búsqueda general relacionada directamente con el proyecto.
         *
         * Esta búsqueda solamente agrega documentos que todavía
         * no hayan sido recuperados mediante las búsquedas
         * específicas.
         */
        String generalQuery =
                buildGeneralQuery(
                        context,
                        parameters
                );

        List<SearchResult> generalResults =
                searchService.search(
                        generalQuery,
                        GENERAL_SEARCH_TOP_K
                );

        addGeneralKnowledge(
                selectedResults,
                usedContent,
                generalResults
        );

        /*
         * Mantener un orden estable de los bloques.
         */
        selectedResults.sort(
                Comparator
                        .comparingInt(
                                (SearchResult result) ->
                                        blockPriority(
                                                result.getChunk()
                                                        .getDocumentName()
                                        )
                        )
                        .thenComparing(
                                SearchResult::getSimilarity,
                                Comparator.reverseOrder()
                        )
        );

        String knowledge =
                buildKnowledge(selectedResults);

        return buildFinalPrompt(
                context,
                parameters,
                knowledge
        );
    }

    /**
     * Construye una consulta específica para un bloque.
     */
    private String buildBlockQuery(
            String blockDescription,
            String context,
            String parameters
    ) {

        return """
                Lean Canvas.

                Bloque que se está investigando:
                %s

                Proyecto:
                %s

                Parámetros:
                %s

                Recuperar conocimiento metodológico y conceptual
                relevante para construir este bloque del Lean Canvas
                aplicado específicamente al proyecto.
                """.formatted(
                blockDescription,
                context,
                parameters
        );
    }

    /**
     * Agrega solamente el mejor resultado del documento
     * solicitado.
     */
    private void addBestResultForDocument(
            List<SearchResult> selectedResults,
            Set<String> usedContent,
            List<SearchResult> results,
            String targetDocument
    ) {

        SearchResult bestResult = null;

        for (SearchResult result : results) {

            if (result == null ||
                    result.getChunk() == null) {
                continue;
            }

            String documentName =
                    normalizeSource(
                            result.getChunk()
                                    .getDocumentName()
                    );

            if (!documentName.equalsIgnoreCase(
                    normalizeSource(targetDocument)
            )) {
                continue;
            }

            String content =
                    normalizeContent(
                            result.getChunk()
                                    .getContent()
                    );

            if (content.isBlank()) {
                continue;
            }

            if (usedContent.contains(content)) {
                continue;
            }

            if (bestResult == null ||
                    result.getSimilarity() >
                            bestResult.getSimilarity()) {

                bestResult = result;
            }
        }

        if (bestResult != null) {

            String content =
                    normalizeContent(
                            bestResult.getChunk()
                                    .getContent()
                    );

            if (usedContent.add(content)) {
                selectedResults.add(bestResult);
            }
        }
    }

    /**
     * Agrega resultados generales solamente cuando pertenecen
     * a documentos que todavía no están presentes.
     */
    private void addGeneralKnowledge(
            List<SearchResult> selectedResults,
            Set<String> usedContent,
            List<SearchResult> results
    ) {

        Set<String> usedDocuments =
                new LinkedHashSet<>();

        for (SearchResult result : results) {

            if (result == null ||
                    result.getChunk() == null) {
                continue;
            }

            String document =
                    normalizeSource(
                            result.getChunk()
                                    .getDocumentName()
                    );

            String content =
                    normalizeContent(
                            result.getChunk()
                                    .getContent()
                    );

            if (document.isBlank() ||
                    content.isBlank()) {
                continue;
            }

            if (usedContent.contains(content)) {
                continue;
            }

            if (containsDocument(
                    selectedResults,
                    document
            )) {
                continue;
            }

            if (usedDocuments.contains(document)) {
                continue;
            }

            selectedResults.add(result);

            usedContent.add(content);
            usedDocuments.add(document);

            if (usedDocuments.size() >=
                    MAX_GENERAL_SOURCES) {
                break;
            }
        }
    }

    private boolean containsDocument(
            List<SearchResult> results,
            String document
    ) {

        for (SearchResult result : results) {

            if (result == null ||
                    result.getChunk() == null) {
                continue;
            }

            String existingDocument =
                    normalizeSource(
                            result.getChunk()
                                    .getDocumentName()
                    );

            if (existingDocument.equalsIgnoreCase(
                    document
            )) {
                return true;
            }
        }

        return false;
    }

    private String buildGeneralQuery(
            String context,
            String parameters
    ) {

        return """
                Lean Canvas para el siguiente proyecto.

                Contexto del proyecto:
                %s

                Parámetros:
                %s

                Buscar conocimiento relevante para construir
                un Lean Canvas completo considerando problema,
                segmentos de clientes, propuesta de valor,
                solución, canales, flujos de ingresos,
                estructura de costes, métricas clave y
                ventaja injusta.
                """.formatted(
                context,
                parameters
        );
    }

    private String buildKnowledge(
            List<SearchResult> results
    ) {

        if (results.isEmpty()) {
            return "No se encontró conocimiento relevante.";
        }

        StringBuilder knowledge =
                new StringBuilder();

        for (SearchResult result : results) {

            if (result == null ||
                    result.getChunk() == null) {
                continue;
            }

            String source =
                    normalizeSource(
                            result.getChunk()
                                    .getDocumentName()
                    );

            String content =
                    normalizeContent(
                            result.getChunk()
                                    .getContent()
                    );

            if (content.isBlank()) {
                continue;
            }

            knowledge.append(
                    "[Fuente: "
            ).append(source)
                    .append("]\n");

            knowledge.append(
                    "[Similitud: "
            ).append(
                    String.format(
                            "%.4f",
                            result.getSimilarity()
                    )
            ).append("]\n");

            knowledge.append(content)
                    .append("\n\n");
        }

        return knowledge.toString().trim();
    }

    private String buildFinalPrompt(
            String context,
            String parameters,
            String knowledge
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

                1. Genera los 9 bloques del Lean Canvas.

                2. Los bloques son:
                   - Problema
                   - Segmentos de clientes
                   - Propuesta de valor única
                   - Solución
                   - Canales
                   - Flujos de ingresos
                   - Estructura de costes
                   - Métricas clave
                   - Ventaja injusta

                3. Utiliza el conocimiento de referencia como
                   orientación metodológica.

                4. Adapta el contenido al contexto y parámetros
                   proporcionados por el usuario.

                5. No copies literalmente el contenido de los
                   documentos de conocimiento.

                6. Evita repetir la misma información entre bloques.

                7. La propuesta de valor debe diferenciarse de
                   la solución.

                8. Los segmentos de clientes deben ser específicos
                   para el proyecto.

                9. Los canales deben ser coherentes con los
                   segmentos de clientes.

                10. Los flujos de ingresos deben ser compatibles
                    con la propuesta y el mercado.

                11. Las métricas deben permitir evaluar el
                    funcionamiento y crecimiento del proyecto.

                12. La ventaja injusta debe representar un elemento
                    difícil de copiar por los competidores. Si no
                    existe información suficiente, indícalo en lugar
                    de inventar una ventaja.

                13. No inventes datos concretos que no estén
                    respaldados por el contexto o los parámetros.

                14. Responde únicamente con el Lean Canvas
                    estructurado en sus 9 bloques.

                Formato esperado:

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
                """.formatted(
                context,
                parameters,
                knowledge
        );
    }

    private int blockPriority(
            String documentName
    ) {

        String source =
                normalizeSource(documentName);

        return switch (source) {

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

            default -> 20;
        };
    }

    private String normalizeSource(
            String source
    ) {

        if (source == null) {
            return "";
        }

        String normalized =
                source.replace("\\", "/");

        return normalized.substring(
                normalized.lastIndexOf("/") + 1
        ).trim();
    }

    private String normalizeContent(
            String content
    ) {

        if (content == null) {
            return "";
        }

        return content
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
    }

    private void validate(
            String context,
            String parameters
    ) {

        if (context == null ||
                context.isBlank()) {

            throw new IllegalArgumentException(
                    "El contexto no puede estar vacío"
            );
        }

        if (parameters == null || 
                parameters.isBlank()) { 
            throw new IllegalArgumentException( 
                "Los parámetros no pueden estar vacíos" 
            ); 
        } 
    } 
}