package com.bmcai.backend.rag.service;

import com.bmcai.backend.embedding.model.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagContextService {

    private final double minSimilarity;
    private final double contextScoreWindow;

    public RagContextService(
            @Value("${rag.search.min-similarity:0.90}")
            double minSimilarity,

            @Value("${rag.search.context-score-window:0.05}")
            double contextScoreWindow
    ) {
        this.minSimilarity = minSimilarity;
        this.contextScoreWindow = contextScoreWindow;
    }

    public boolean hasRelevantContext(
            List<SearchResult> results
    ) {

        if (results == null || results.isEmpty()) {
            return false;
        }

        SearchResult bestResult = results.get(0);

        if (bestResult == null) {
            return false;
        }

        return bestResult.getSimilarity()
                >= minSimilarity;
    }

    public List<SearchResult> getRelevantResults(
            List<SearchResult> results
    ) {

        if (!hasRelevantContext(results)) {
            return List.of();
        }

        double bestSimilarity =
                results.get(0).getSimilarity();

        double minimumContextSimilarity =
                bestSimilarity - contextScoreWindow;

        return results.stream()
                .filter(result ->
                        result != null &&
                        result.getChunk() != null &&
                        result.getSimilarity()
                                >= minimumContextSimilarity
                )
                .toList();
    }

    public String buildContext(
            List<SearchResult> results
    ) {

        List<SearchResult> relevantResults =
                getRelevantResults(results);

        if (relevantResults.isEmpty()) {
            return "";
        }

        StringBuilder context =
                new StringBuilder();

        for (SearchResult result :
                relevantResults) {

            context.append("[Fuente: ")
                    .append(
                            result.getChunk()
                                    .getDocumentPath()
                    )
                    .append("]\n");

            context.append("[Similitud: ")
                    .append(
                            String.format(
                                    "%.4f",
                                    result.getSimilarity()
                            )
                    )
                    .append("]\n");

            context.append(
                    result.getChunk().getContent()
            );

            context.append("\n\n");
        }

        return context.toString().trim();
    }

    public int countRelevantResults(
            List<SearchResult> results
    ) {
        return getRelevantResults(results).size();
    }

    public double getMinSimilarity() {
        return minSimilarity;
    }

    public double getContextScoreWindow() {
        return contextScoreWindow;
    }
}