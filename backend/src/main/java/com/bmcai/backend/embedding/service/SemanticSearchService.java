package com.bmcai.backend.embedding.service;

import com.bmcai.backend.embedding.model.ChunkEmbedding;
import com.bmcai.backend.embedding.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SemanticSearchService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public SemanticSearchService(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    public List<SearchResult> search(
            String query,
            int topK
    ) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "La consulta no puede estar vacía"
            );
        }

        if (topK <= 0) {
            throw new IllegalArgumentException(
                    "topK debe ser mayor que cero"
            );
        }

        float[] queryEmbedding =
                embeddingService.generateQueryEmbedding(
                        query
                );

        List<SearchResult> results =
                new ArrayList<>();

        for (ChunkEmbedding chunk :
                vectorStoreService.getAll()) {

            double similarity =
                    embeddingService.cosineSimilarity(
                            queryEmbedding,
                            chunk.getEmbedding()
                    );

            results.add(
                    new SearchResult(
                            chunk,
                            similarity
                    )
            );
        }

        results.sort(
                Comparator.comparingDouble(
                        SearchResult::getSimilarity
                ).reversed()
        );

        return results.stream()
                .limit(topK)
                .toList();
    }
}