package com.bmcai.backend.embedding.service;

import com.bmcai.backend.embedding.model.ChunkEmbedding;
import com.bmcai.backend.knowledge.model.KnowledgeChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingChunkService {

    private final EmbeddingService embeddingService;

    public EmbeddingChunkService(
            EmbeddingService embeddingService
    ) {
        this.embeddingService = embeddingService;
    }

    public ChunkEmbedding createEmbedding(
            KnowledgeChunk chunk
    ) {

        if (chunk == null) {
            throw new IllegalArgumentException(
                    "El chunk no puede ser null"
            );
        }

        float[] embedding =
                embeddingService.generatePassageEmbedding(
                        chunk.getContent()
                );

        return new ChunkEmbedding(
                chunk.getDocumentName(),
                chunk.getDocumentPath(),
                chunk.getDocumentType(),
                chunk.getChunkIndex(),
                chunk.getContent(),
                embedding
        );
    }

    public List<ChunkEmbedding> createEmbeddings(
            List<KnowledgeChunk> chunks
    ) {

        List<ChunkEmbedding> embeddings =
                new ArrayList<>();

        if (chunks == null || chunks.isEmpty()) {
            return embeddings;
        }

        for (KnowledgeChunk chunk : chunks) {

            embeddings.add(
                    createEmbedding(chunk)
            );
        }

        return embeddings;
    }
}