package com.bmcai.backend.embedding.service;

import com.bmcai.backend.embedding.model.ChunkEmbedding;
import com.bmcai.backend.knowledge.model.KnowledgeChunk;
import com.bmcai.backend.knowledge.model.KnowledgeDocument;
import com.bmcai.backend.knowledge.service.KnowledgeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeEmbeddingIndexService {

    private final KnowledgeService knowledgeService;
    private final EmbeddingChunkService embeddingChunkService;
    private final VectorStoreService vectorStoreService;

    public KnowledgeEmbeddingIndexService(
            KnowledgeService knowledgeService,
            EmbeddingChunkService embeddingChunkService,
            VectorStoreService vectorStoreService
    ) {
        this.knowledgeService = knowledgeService;
        this.embeddingChunkService =
                embeddingChunkService;
        this.vectorStoreService =
                vectorStoreService;
    }

    public int buildIndex() {

        vectorStoreService.clear();

        List<KnowledgeDocument> documents =
                knowledgeService.listDocuments();

        int totalChunks = 0;

        for (KnowledgeDocument document : documents) {

            List<KnowledgeChunk> chunks =
                    knowledgeService.chunkDocument(
                            document.getPath()
                    );

            List<ChunkEmbedding> embeddings =
                    embeddingChunkService.createEmbeddings(
                            chunks
                    );

            vectorStoreService.addAll(embeddings);

            totalChunks += embeddings.size();
        }

        return totalChunks;
    }

    public int getIndexedChunks() {
        return vectorStoreService.size();
    }
}