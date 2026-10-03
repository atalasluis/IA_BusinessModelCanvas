package com.bmcai.backend.generator.service;

import com.bmcai.backend.generator.model.GenerateRequest;
import com.bmcai.backend.generator.model.GenerateResponse;
import com.bmcai.backend.generator.model.GenerateLeanResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String geminiUrl;

    private final ObjectMapper objectMapper;

    public GeminiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GenerateResponse callGemini(GenerateRequest request) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // ====================================================================
        // PASO 1: Consultar el endpoint RAG de tu compañero
        // ====================================================================
        String ragUrl = "http://localhost:8080/api/bmc/context";
        
        // Armamos el body exacto que espera su controlador
        String ragRequestBody = objectMapper.writeValueAsString(Map.of(
            "context", request.context(),
            "parameters", request.parameters()
        ));

        HttpRequest ragHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ragUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(ragRequestBody))
                .build();

        // Hacemos la llamada interna al puerto 8080
        HttpResponse<String> ragResponse = client.send(ragHttpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (ragResponse.statusCode() != 200) {
            throw new RuntimeException("El servicio RAG falló. Revisa los logs del compañero: " + ragResponse.body());
        }

        // Extraemos el "prompt" gigante que contiene la teoría de los PDFs
        JsonNode ragNode = objectMapper.readTree(ragResponse.body());
        String promptEnriquecido = ragNode.path("prompt").asText();


        // ====================================================================
        // PASO 2: Enviar el prompt enriquecido a Gemini con JSON Schema
        // ====================================================================
        String systemInstruction = """
            Eres un estratega de negocios experto. Analiza ideas con 'Jobs To Be Done' y estructurar un Business Model Canvas.
            Utiliza estrictamente la información proporcionada en el CONOCIMIENTO RECUPERADO para fundamentar tu respuesta.
            """;

        String schemaJson = """
        {
          "type": "OBJECT",
          "properties": {
            "jtbd_analysis": {
              "type": "OBJECT",
              "properties": {
                "job_principal": { "type": "STRING" },
                "job_funcional": { "type": "STRING" },
                "job_social": { "type": "STRING" },
                "job_emocional": { "type": "STRING" },
                "necesidades": { "type": "ARRAY", "items": { "type": "STRING" } },
                "dolores": { "type": "ARRAY", "items": { "type": "STRING" } },
                "beneficios": { "type": "ARRAY", "items": { "type": "STRING" } },
                "resultado_esperado": { "type": "STRING" }
              },
              "required": ["job_principal", "job_funcional", "job_social", "job_emocional", "necesidades", "dolores", "beneficios", "resultado_esperado"]
            },
            "business_model_canvas": {
              "type": "OBJECT",
              "properties": {
                "alianzas_clave": { "type": "ARRAY", "items": { "type": "STRING" } },
                "actividades_clave": { "type": "ARRAY", "items": { "type": "STRING" } },
                "recursos_clave": { "type": "ARRAY", "items": { "type": "STRING" } },
                "propuesta_valor": { "type": "ARRAY", "items": { "type": "STRING" } },
                "relacion_clientes": { "type": "ARRAY", "items": { "type": "STRING" } },
                "canales": { "type": "ARRAY", "items": { "type": "STRING" } },
                "segmentos_clientes": { "type": "ARRAY", "items": { "type": "STRING" } },
                "estructura_costos": { "type": "ARRAY", "items": { "type": "STRING" } },
                "fuentes_ingresos": { "type": "ARRAY", "items": { "type": "STRING" } }
              },
              "required": ["alianzas_clave", "actividades_clave", "recursos_clave", "propuesta_valor", "relacion_clientes", "canales", "segmentos_clientes", "estructura_costos", "fuentes_ingresos"]
            }
          },
          "required": ["jtbd_analysis", "business_model_canvas"]
        }
        """;

        JsonNode schemaNode = objectMapper.readTree(schemaJson);

        Map<String, Object> geminiRequestBody = Map.of(
            "systemInstruction", Map.of("parts", Map.of("text", systemInstruction)),
            "contents", Map.of("parts", Map.of("text", promptEnriquecido)), // Inyectamos el resultado del RAG aquí
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", schemaNode
            )
        );

        String jsonBody = objectMapper.writeValueAsString(geminiRequestBody);

        HttpRequest geminiHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(geminiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(geminiHttpRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode rootNode = objectMapper.readTree(response.body());

        if (rootNode.has("error")) {
            throw new RuntimeException("Error desde Google: " + rootNode.path("error").path("message").asText());
        }

        JsonNode candidates = rootNode.path("candidates");
        if (candidates.isMissingNode() || !candidates.has(0)) {
            throw new RuntimeException("Respuesta bloqueada o inesperada: " + response.body());
        }

        String geminiJsonOutput = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        return objectMapper.readValue(geminiJsonOutput, GenerateResponse.class);
    }

    public GenerateLeanResponse callGeminiForLean(GenerateRequest request) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // 1. Reutilizamos la consulta al endpoint RAG de tu compañero
        String ragUrl = "http://localhost:8080/api/bmc/context";
        String ragRequestBody = objectMapper.writeValueAsString(Map.of(
            "context", request.context(),
            "parameters", request.parameters()
        ));

        HttpRequest ragHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ragUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(ragRequestBody))
                .build();

        HttpResponse<String> ragResponse = client.send(ragHttpRequest, HttpResponse.BodyHandlers.ofString());
        if (ragResponse.statusCode() != 200) {
            throw new RuntimeException("El servicio RAG falló: " + ragResponse.body());
        }

        JsonNode ragNode = objectMapper.readTree(ragResponse.body());
        String promptEnriquecido = ragNode.path("prompt").asText();

        // 2. Instrucciones específicas para Ash Maurya y Riesgo
        String systemInstruction = """
            Eres un experto en metodologías ágiles y Lean Startup (Ash Maurya). Analiza la idea y estructura un Lean Canvas estricto.
            REGLA CRÍTICA PARA 'problemas': 
            1. DEBES incluir datos numéricos en CADA problema formulado (ej. 'El 75% de los usuarios...' o 'De 100 entrevistados, 65...').
            2. Al final de la descripción de cada problema, DEBES incluir obligatoriamente un enlace web de respaldo usando el formato '[Fuente: URL]'.
            ADVERTENCIA DE ALUCINACIÓN: Si el usuario o el CONOCIMIENTO RECUPERADO no proporcionan URLs, extrae URLs 100% reales y válidas de instituciones oficiales, ministerios, o consultoras (ej. OMS, Gartner, Forbes) de tu conocimiento preentrenado. BAJO NINGUNA CIRCUNSTANCIA inventes URLs inexistentes. Si no tienes un enlace exacto al estudio, proporciona la URL de la página principal de la institución real que respalde la temática.
            """;

        // 3. El nuevo JSON Schema blindado para Lean Canvas
        String schemaJson = """
        {
          "type": "OBJECT",
          "properties": {
            "lean_canvas": {
              "type": "OBJECT",
              "properties": {
                "problemas": { "type": "ARRAY", "items": { "type": "STRING" } },
                "alternativas_existentes": { "type": "ARRAY", "items": { "type": "STRING" } },
                "solucion": { "type": "ARRAY", "items": { "type": "STRING" } },
                "metricas_clave": { "type": "ARRAY", "items": { "type": "STRING" } },
                "propuesta_valor_unica": { "type": "ARRAY", "items": { "type": "STRING" } },
                "concepto_alto_nivel": { "type": "ARRAY", "items": { "type": "STRING" } },
                "ventaja_injusta": { "type": "ARRAY", "items": { "type": "STRING" } },
                "canales": { "type": "ARRAY", "items": { "type": "STRING" } },
                "segmentos_clientes": { "type": "ARRAY", "items": { "type": "STRING" } },
                "early_adopters": { "type": "ARRAY", "items": { "type": "STRING" } },
                "estructura_costos": { "type": "ARRAY", "items": { "type": "STRING" } },
                "fuentes_ingresos": { "type": "ARRAY", "items": { "type": "STRING" } }
              },
              "required": ["problemas", "alternativas_existentes", "solucion", "metricas_clave", "propuesta_valor_unica", "concepto_alto_nivel", "ventaja_injusta", "canales", "segmentos_clientes", "early_adopters", "estructura_costos", "fuentes_ingresos"]
            }
          },
          "required": ["lean_canvas"]
        }
        """;

        JsonNode schemaNode = objectMapper.readTree(schemaJson);

        Map<String, Object> geminiRequestBody = Map.of(
            "systemInstruction", Map.of("parts", Map.of("text", systemInstruction)),
            "contents", Map.of("parts", Map.of("text", promptEnriquecido)),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", schemaNode
            )
        );

        String jsonBody = objectMapper.writeValueAsString(geminiRequestBody);
        HttpRequest geminiHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(geminiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(geminiHttpRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode rootNode = objectMapper.readTree(response.body());

        if (rootNode.has("error")) {
            throw new RuntimeException("Error desde Google: " + rootNode.path("error").path("message").asText());
        }

        JsonNode candidates = rootNode.path("candidates");
        if (candidates.isMissingNode() || !candidates.has(0)) {
            throw new RuntimeException("Respuesta inesperada: " + response.body());
        }

        String geminiJsonOutput = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        return objectMapper.readValue(geminiJsonOutput, GenerateLeanResponse.class);
    } 
}
