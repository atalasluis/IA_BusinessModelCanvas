package com.bmcai.backend.rag.service;

import com.bmcai.backend.embedding.model.SearchResult;
import com.bmcai.backend.embedding.service.SemanticSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private final SemanticSearchService semanticSearchService;
    private final RagContextService ragContextService;
    private final int defaultTopK;

    public RagService(
            SemanticSearchService semanticSearchService,
            RagContextService ragContextService,
            @Value("${rag.search.top-k:5}") int defaultTopK
    ) {
        this.semanticSearchService =
                semanticSearchService;

        this.ragContextService =
                ragContextService;

        this.defaultTopK = defaultTopK;
    }

    public RagResponse retrieve(
            String query
    ) {

        return retrieve(
                query,
                defaultTopK
        );
    }

    public RagResponse retrieve(
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

        List<SearchResult> results =
                semanticSearchService.search(
                        query,
                        topK
                );

        boolean relevant =
                ragContextService.hasRelevantContext(
                        results
                );

        int relevantResults =
                ragContextService.countRelevantResults(
                        results
                );

        String context =
                relevant
                        ? ragContextService.buildContext(
                                results
                        )
                        : "";

        return new RagResponse(
                query,
                topK,
                results,
                relevant,
                context,
                relevantResults
        );
    }

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public static class RagResponse {

        private final String query;
        private final int topK;
        private final List<SearchResult> results;
        private final boolean relevant;
        private final String context;
        private final int relevantResults;

        public RagResponse(
            String query,
            int topK,
            List<SearchResult> results,
            boolean relevant,
            String context,
            int relevantResults
        ) {
            this.query = query;
            this.topK = topK;
            this.results = results;
            this.relevant = relevant;
            this.context = context;
            this.relevantResults = relevantResults;
        }

        public String getQuery() {
            return query;
        }

        public int getTopK() {
            return topK;
        }

        public List<SearchResult> getResults() {
            return results;
        }

        public boolean isRelevant() {
            return relevant;
        }

        public String getContext() {
            return context;
        }

        public int getRelevantResults() {
            return relevantResults;
        }
    }
}