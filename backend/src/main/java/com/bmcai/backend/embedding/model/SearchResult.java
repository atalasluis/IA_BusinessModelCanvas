package com.bmcai.backend.embedding.model;

public class SearchResult {

    private ChunkEmbedding chunk;
    private double similarity;

    public SearchResult() {
    }

    public SearchResult(
            ChunkEmbedding chunk,
            double similarity
    ) {
        this.chunk = chunk;
        this.similarity = similarity;
    }

    public ChunkEmbedding getChunk() {
        return chunk;
    }

    public void setChunk(ChunkEmbedding chunk) {
        this.chunk = chunk;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}