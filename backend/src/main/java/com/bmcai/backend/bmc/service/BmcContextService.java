package com.bmcai.backend.bmc.service;

import com.bmcai.backend.embedding.model.SearchResult;
import com.bmcai.backend.rag.service.RagService;
import com.bmcai.backend.rag.service.RagService.RagResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BmcContextService {

    private final RagService ragService;

    /*
     * Número de resultados recuperados para cada consulta
     * específica del BMC/JTBD.
     */
    private static final int SEARCH_TOP_K = 8;

    /*
     * Número de resultados utilizados en la búsqueda general.
     *
     * Esta búsqueda permite recuperar:
     *
     * - BMC
     * - JTBD
     * - libros
     * - otros documentos de KNOWLEDGE
     */
    private static final int GENERAL_SEARCH_TOP_K = 15;

    /*
     * Cantidad máxima de documentos adicionales que pueden
     * incorporarse mediante la búsqueda general.
     *
     * No significa que se excluyan los libros.
     *
     * Simplemente evita que demasiados documentos adicionales
     * hagan crecer innecesariamente el prompt.
     */
    private static final int MAX_GENERAL_SOURCES = 8;

    /*
     * Los nueve bloques oficiales del Business Model Canvas.
     *
     * Cada bloque tiene:
     *
     * - una consulta semántica descriptiva
     * - el documento específico que queremos recuperar
     */
    private static final List<BmcBlockQuery> BMC_BLOCK_QUERIES =
            List.of(

                    new BmcBlockQuery(
                            "Segmentos de clientes necesidades problemas características comportamiento",
                            "bmc/segmentos-clientes.md"
                    ),

                    new BmcBlockQuery(
                            "Propuesta de valor problemas necesidades beneficios dolores resultados solución",
                            "bmc/propuesta-valor.md"
                    ),

                    new BmcBlockQuery(
                            "Canales comunicación venta entrega acceso propuesta de valor clientes",
                            "bmc/canales.md"
                    ),

                    new BmcBlockQuery(
                            "Relaciones con clientes atención interacción soporte confianza automatización",
                            "bmc/relaciones-clientes.md"
                    ),

                    new BmcBlockQuery(
                            "Fuentes de ingresos modelo de ingresos cobro clientes pagos monetización",
                            "bmc/fuentes-ingresos.md"
                    ),

                    new BmcBlockQuery(
                            "Recursos clave activos humanos intelectuales físicos financieros necesarios",
                            "bmc/recursos-clave.md"
                    ),

                    new BmcBlockQuery(
                            "Actividades clave acciones necesarias operaciones desarrollo marketing ventas actividades",
                            "bmc/actividades-clave.md"
                    ),

                    new BmcBlockQuery(
                            "Socios clave proveedores aliados asociaciones recursos actividades colaboración",
                            "bmc/socios-clave.md"
                    ),

                    new BmcBlockQuery(
                            "Estructura de costos costos fijos variables directos indirectos operación",
                            "bmc/estructura-costos.md"
                    )
            );

    /*
     * Documentos JTBD utilizados como conocimiento complementario.
     */
    private static final List<BmcSupplementaryQuery> SUPPLEMENTARY_QUERIES =
            List.of(

                    new BmcSupplementaryQuery(
                            "Customer Profile Jobs Pains Gains necesidades problemas beneficios cliente",
                            "jtbd/customer-profile.md"
                    ),

                    new BmcSupplementaryQuery(
                            "Value Map productos servicios pain relievers gain creators propuesta de valor",
                            "jtbd/value-map.md"
                    )
            );

    public BmcContextService(RagService ragService) {
        this.ragService = ragService;
    }

    public String buildPrompt(
            String context,
            String parameters
    ) {

        validate(
                context,
                parameters
        );

        /*
         * Cada fuente aparece como máximo una vez.
         *
         * La clave es la ruta normalizada del documento.
         */
        Map<String, KnowledgeResult> bestBySource =
                new HashMap<>();

        /*
         * ---------------------------------------------------------
         * 1. BMC GENERAL
         * ---------------------------------------------------------
         */
        RagResponse bmcGeneralResponse =
                ragService.retrieve(
                        "Business Model Canvas nueve bloques estructura relaciones modelo de negocio",
                        SEARCH_TOP_K
                );

        SearchResult bmcGeneralResult =
                findTargetDocument(
                        bmcGeneralResponse.getResults(),
                        "bmc/business-model-canvas.md"
                );

        if (bmcGeneralResult != null) {

            addBestResult(
                    bestBySource,
                    createKnowledgeResult(
                            bmcGeneralResult
                    )
            );
        }

        /*
         * ---------------------------------------------------------
         * 2. NUEVE BLOQUES DEL BMC
         * ---------------------------------------------------------
         *
         * Estos documentos son conocimiento estructural obligatorio
         * para que la IA pueda construir correctamente el Canvas.
         */
        for (BmcBlockQuery blockQuery :
                BMC_BLOCK_QUERIES) {

            RagResponse response =
                    ragService.retrieve(
                            blockQuery.query(),
                            SEARCH_TOP_K
                    );

            SearchResult targetResult =
                    findTargetDocument(
                            response.getResults(),
                            blockQuery.expectedSource()
                    );

            if (targetResult == null) {
                continue;
            }

            addBestResult(
                    bestBySource,
                    createKnowledgeResult(
                            targetResult
                    )
            );
        }

        /*
         * ---------------------------------------------------------
         * 3. JTBD
         * ---------------------------------------------------------
         */
        for (BmcSupplementaryQuery supplementaryQuery :
                SUPPLEMENTARY_QUERIES) {

            RagResponse response =
                    ragService.retrieve(
                            supplementaryQuery.query(),
                            SEARCH_TOP_K
                    );

            SearchResult targetResult =
                    findTargetDocument(
                            response.getResults(),
                            supplementaryQuery.expectedSource()
                    );

            if (targetResult == null) {
                continue;
            }

            addBestResult(
                    bestBySource,
                    createKnowledgeResult(
                            targetResult
                    )
            );
        }

        /*
         * ---------------------------------------------------------
         * 4. BÚSQUEDA GENERAL
         * ---------------------------------------------------------
         *
         * Esta es la parte importante de la mejora.
         *
         * Aquí NO filtramos por:
         *
         *     bmc/
         *     jtbd/
         *
         * Por lo tanto, también pueden aparecer:
         *
         *     libros/
         *     otros documentos/
         *     otros tipos de KNOWLEDGE
         *
         * El resultado se limita posteriormente por documento para
         * evitar que un único libro ocupe todo el contexto.
         */
        String generalQuery =
                buildGeneralQuery(
                        context,
                        parameters
                );

        RagResponse generalResponse =
                ragService.retrieve(
                        generalQuery,
                        GENERAL_SEARCH_TOP_K
                );

        addGeneralKnowledge(
                bestBySource,
                generalResponse.getResults()
        );

        /*
         * ---------------------------------------------------------
         * 5. CONVERTIR A LISTA
         * ---------------------------------------------------------
         */
        List<KnowledgeResult> finalResults =
                new ArrayList<>(
                        bestBySource.values()
                );

        /*
         * ---------------------------------------------------------
         * 6. ORDENAR FUENTES
         * ---------------------------------------------------------
         *
         * Primero:
         *
         * 1. BMC general
         * 2. nueve bloques BMC
         * 3. JTBD
         * 4. libros y demás KNOWLEDGE
         */
        finalResults.sort(
                Comparator
                        .comparingInt(
                                (KnowledgeResult result) ->
                                        sourcePriority(
                                                result.source()
                                        )
                        )
                        .thenComparingInt(
                                result ->
                                        blockPriority(
                                                result.source()
                                        )
                        )
                        .thenComparing(
                                KnowledgeResult::similarity,
                                Comparator.reverseOrder()
                        )
        );

        /*
         * ---------------------------------------------------------
         * 7. ELIMINAR CONTENIDO DUPLICADO
         * ---------------------------------------------------------
         */
        List<KnowledgeResult> uniqueResults =
                removeDuplicateContent(
                        finalResults
                );

        /*
         * ---------------------------------------------------------
         * 8. CONSTRUIR CONOCIMIENTO
         * ---------------------------------------------------------
         */
        StringBuilder knowledge =
                new StringBuilder();

        for (KnowledgeResult result :
                uniqueResults) {

            knowledge.append(
                    result.formattedContent()
            );

            knowledge.append("\n\n");
        }

        /*
         * ---------------------------------------------------------
         * 9. CONSTRUIR PROMPT
         * ---------------------------------------------------------
         */
        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                "CONTEXTO DEL USUARIO:\n"
        );

        prompt.append(
                context.trim()
        );

        prompt.append("\n\n");

        prompt.append(
                "PARÁMETROS:\n"
        );

        prompt.append(
                parameters.trim()
        );

        prompt.append("\n\n");

        prompt.append(
                "CONOCIMIENTO RECUPERADO:\n"
        );

        if (knowledge.isEmpty()) {

            prompt.append(
                    "No se encontró conocimiento relevante "
                            + "en la base de conocimiento."
            );

        } else {

            prompt.append(
                    knowledge.toString().trim()
            );
        }

        prompt.append("\n\n");

        prompt.append(
                "INSTRUCCIONES:\n"
        );

        prompt.append(
                "Genera un Business Model Canvas utilizando "
                        + "el contexto y los parámetros proporcionados "
                        + "por el usuario y el conocimiento recuperado. "
                        + "Analiza la información disponible y formula "
                        + "una propuesta coherente para los nueve bloques "
                        + "del Business Model Canvas: "
                        + "segmentos de clientes, propuesta de valor, "
                        + "canales, relaciones con clientes, "
                        + "fuentes de ingresos, recursos clave, "
                        + "actividades clave, socios clave y "
                        + "estructura de costos. "
                        + "Relaciona los bloques entre sí y evita "
                        + "tratar cada bloque como una lista independiente. "
                        + "Utiliza los documentos BMC y JTBD como marco "
                        + "metodológico. "
                        + "Utiliza también los libros y demás fuentes "
                        + "recuperadas cuando aporten información "
                        + "relevante para el análisis. "
                        + "No descartes una fuente únicamente por "
                        + "pertenecer a un libro o documento diferente. "
                        + "El conocimiento recuperado describe "
                        + "metodologías, conceptos y referencias y no "
                        + "debe tratarse automáticamente como información "
                        + "específica del negocio del usuario. "
                        + "No inventes hechos sobre el mercado, clientes "
                        + "o negocio cuando no estén presentes en el "
                        + "contexto o parámetros. "
                        + "Cuando la información del usuario no sea "
                        + "suficiente, formula propuestas como hipótesis "
                        + "y no como hechos. "
                        + "Utiliza JTBD como apoyo para relacionar "
                        + "segmentos, problemas, necesidades, beneficios "
                        + "y propuesta de valor."
        );

        return prompt.toString();
    }

    /*
     * Construye la consulta general utilizando el problema real
     * planteado por el usuario.
     */
    private String buildGeneralQuery(
            String context,
            String parameters
    ) {

        return context.trim()
                + " "
                + parameters.trim()
                + " "
                + "modelo de negocio propuesta de valor clientes "
                + "necesidades problemas soluciones mercado estrategia "
                + "innovación plataforma";
    }

    /*
     * Agrega resultados de la búsqueda general.
     *
     * Importante:
     *
     * - No filtra libros.
     * - No filtra por carpeta.
     * - No permite que varios chunks del mismo documento
     *   ocupen todo el contexto.
     */
    private void addGeneralKnowledge(
            Map<String, KnowledgeResult> bestBySource,
            List<SearchResult> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return;
        }

        /*
         * Primero agrupamos los resultados por documento.
         */
        Map<String, SearchResult> bestGeneralBySource =
                new HashMap<>();

        for (SearchResult result :
                results) {

            if (result == null ||
                    result.getChunk() == null) {

                continue;
            }

            String source =
                    result.getChunk()
                            .getDocumentPath();

            if (source == null ||
                    source.isBlank()) {

                continue;
            }

            String normalizedSource =
                    normalizeSource(source);

            SearchResult current =
                    bestGeneralBySource.get(
                            normalizedSource
                    );

            if (current == null ||
                    result.getSimilarity()
                            > current.getSimilarity()) {

                bestGeneralBySource.put(
                        normalizedSource,
                        result
                );
            }
        }

        /*
         * Ordenar los documentos encontrados por similitud.
         */
        List<SearchResult> uniqueGeneralResults =
                new ArrayList<>(
                        bestGeneralBySource.values()
                );

        uniqueGeneralResults.sort(
                Comparator.comparingDouble(
                        SearchResult::getSimilarity
                ).reversed()
        );

        /*
         * Contar únicamente las fuentes nuevas.
         *
         * Si un documento ya fue recuperado por las búsquedas
         * específicas BMC/JTBD, no consume uno de los espacios
         * adicionales.
         */
        int addedSources = 0;

        for (SearchResult result :
                uniqueGeneralResults) {

            String source =
                    result.getChunk()
                            .getDocumentPath();

            String normalizedSource =
                    normalizeSource(source);

            /*
             * Ya tenemos este documento gracias a las búsquedas
             * específicas.
             */
            if (bestBySource.containsKey(
                    normalizedSource
            )) {
                continue;
            }

            KnowledgeResult knowledgeResult =
                    createKnowledgeResult(
                            result
                    );

            addBestResult(
                    bestBySource,
                    knowledgeResult
            );

            addedSources++;

            if (addedSources >=
                    MAX_GENERAL_SOURCES) {

                break;
            }
        }
    }

    /*
     * Busca un documento específico dentro de los resultados.
     */
    private SearchResult findTargetDocument(
            List<SearchResult> results,
            String expectedSource
    ) {

        if (results == null ||
                results.isEmpty()) {

            return null;
        }

        String normalizedExpectedSource =
                normalizeSource(
                        expectedSource
                );

        SearchResult bestMatch = null;

        for (SearchResult result :
                results) {

            if (result == null ||
                    result.getChunk() == null) {

                continue;
            }

            String documentPath =
                    result.getChunk()
                            .getDocumentPath();

            if (documentPath == null ||
                    documentPath.isBlank()) {

                continue;
            }

            String normalizedDocumentPath =
                    normalizeSource(
                            documentPath
                    );

            if (!normalizedDocumentPath.equals(
                    normalizedExpectedSource
            )) {
                continue;
            }

            if (bestMatch == null ||
                    result.getSimilarity()
                            > bestMatch.getSimilarity()) {

                bestMatch = result;
            }
        }

        return bestMatch;
    }

    /*
     * Convierte SearchResult a nuestro resultado interno.
     */
    private KnowledgeResult createKnowledgeResult(
            SearchResult result
    ) {

        String source =
                result.getChunk()
                        .getDocumentPath();

        String content =
                result.getChunk()
                        .getContent();

        String formattedContent =
                "[Fuente: "
                        + source
                        + "]\n"
                        + "[Similitud: "
                        + result.getSimilarity()
                        + "]\n"
                        + content;

        return new KnowledgeResult(
                source,
                result.getSimilarity(),
                content,
                formattedContent
        );
    }

    /*
     * Conserva el resultado de mayor similitud para cada fuente.
     */
    private void addBestResult(
            Map<String, KnowledgeResult> bestBySource,
            KnowledgeResult candidate
    ) {

        String normalizedSource =
                normalizeSource(
                        candidate.source()
                );

        KnowledgeResult current =
                bestBySource.get(
                        normalizedSource
                );

        if (current == null ||
                candidate.similarity()
                        > current.similarity()) {

            bestBySource.put(
                    normalizedSource,
                    candidate
            );
        }
    }

    /*
     * Prioridad general de las fuentes.
     */
    private int sourcePriority(
            String source
    ) {

        String normalized =
                normalizeSource(source);

        if (normalized.equals(
                "bmc/business-model-canvas.md"
        )) {
            return 0;
        }

        if (normalized.startsWith("bmc/")) {
            return 1;
        }

        if (normalized.startsWith("jtbd/")) {
            return 2;
        }

        /*
         * Libros y cualquier otra fuente de KNOWLEDGE
         * permanecen dentro del contexto.
         */
        return 3;
    }

    /*
     * Orden lógico de los documentos.
     */
    private int blockPriority(
            String source
    ) {

        String normalized =
                normalizeSource(source);

        return switch (normalized) {

            case "bmc/segmentos-clientes.md" ->
                    0;

            case "bmc/propuesta-valor.md" ->
                    1;

            case "bmc/canales.md" ->
                    2;

            case "bmc/relaciones-clientes.md" ->
                    3;

            case "bmc/fuentes-ingresos.md" ->
                    4;

            case "bmc/recursos-clave.md" ->
                    5;

            case "bmc/actividades-clave.md" ->
                    6;

            case "bmc/socios-clave.md" ->
                    7;

            case "bmc/estructura-costos.md" ->
                    8;

            case "jtbd/customer-profile.md" ->
                    9;

            case "jtbd/value-map.md" ->
                    10;

            default ->
                    99;
        };
    }

    /*
     * Elimina contenido idéntico aunque provenga de fuentes
     * diferentes.
     */
    private List<KnowledgeResult> removeDuplicateContent(
            List<KnowledgeResult> results
    ) {

        List<KnowledgeResult> unique =
                new ArrayList<>();

        Set<String> seenContent =
                new HashSet<>();

        for (KnowledgeResult candidate :
                results) {

            String normalizedContent =
                    normalizeContent(
                            candidate.content()
                    );

            if (seenContent.add(
                    normalizedContent
            )) {

                unique.add(candidate);
            }
        }

        return unique;
    }

    /*
     * Normaliza rutas.
     *
     * libros\La-Vaca-Púrpura...
     * ->
     * libros/la-vaca-púrpura...
     */
    private String normalizeSource(
            String source
    ) {

        if (source == null) {
            return "";
        }

        return source
                .replace('\\', '/')
                .trim()
                .toLowerCase();
    }

    /*
     * Normaliza contenido para detectar duplicados.
     */
    private String normalizeContent(
            String content
    ) {

        if (content == null) {
            return "";
        }

        return content
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    /*
     * Validación de entrada.
     */
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

    /*
     * Consulta de un bloque BMC.
     */
    private record BmcBlockQuery(
            String query,
            String expectedSource
    ) {
    }

    /*
     * Consulta complementaria JTBD.
     */
    private record BmcSupplementaryQuery(
            String query,
            String expectedSource
    ) {
    }

    /*
     * Resultado interno utilizado para construir el prompt.
     */
    private record KnowledgeResult(
            String source,
            double similarity,
            String content,
            String formattedContent
    ) {
    }
}