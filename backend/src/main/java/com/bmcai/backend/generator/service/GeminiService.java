package com.bmcai.backend.generator.service;

import com.bmcai.backend.generator.model.GenerateRequest;
import com.bmcai.backend.generator.model.GenerateResponse;
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
        String systemInstruction = """
            Eres un estratega de negocios experto. Analiza ideas con 'Jobs To Be Done' y estructurar un Business Model Canvas.
            Aplica conceptos de Seth Godin (diferenciación, tribus), Michael Porter (ventaja competitiva) y Philip Kotler (Marketing 7.0).
            """;

        String userPrompt = "Contexto: " + request.context() + "\nParámetros: " + request.parameters();

        // 1. Definimos el JSON Schema estricto del que Gemini NO puede escapar
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

        // 2. Inyectamos el Schema en la configuración de generación
        Map<String, Object> requestBody = Map.of(
            "systemInstruction", Map.of("parts", Map.of("text", systemInstruction)),
            "contents", Map.of("parts", Map.of("text", userPrompt)),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", schemaNode
            )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(geminiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
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

}
