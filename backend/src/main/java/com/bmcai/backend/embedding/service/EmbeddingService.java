package com.bmcai.backend.embedding.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "El texto no puede estar vacío"
            );
        }

        return embeddingModel.embed(text);
    }

    public float[] generateQueryEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "La consulta no puede estar vacía"
            );
        }

        return embeddingModel.embed(
                "query: " + text
        );
    }

    public float[] generatePassageEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido no puede estar vacío"
            );
        }

        return embeddingModel.embed(
                "passage: " + text
        );
    }

    public double cosineSimilarity(
            float[] vectorA,
            float[] vectorB
    ) {

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión"
            );
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {

            dotProduct +=
                    (double) vectorA[i] * vectorB[i];

            normA +=
                    (double) vectorA[i] * vectorA[i];

            normB +=
                    (double) vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct /
                (Math.sqrt(normA) * Math.sqrt(normB));
    }
}