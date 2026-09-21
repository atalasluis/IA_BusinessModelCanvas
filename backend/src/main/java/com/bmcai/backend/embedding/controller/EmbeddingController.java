package com.bmcai.backend.embedding.controller;

import com.bmcai.backend.embedding.model.ChunkEmbedding;
import com.bmcai.backend.embedding.service.EmbeddingChunkService;
import com.bmcai.backend.embedding.service.EmbeddingService;
import com.bmcai.backend.knowledge.model.KnowledgeChunk;
import com.bmcai.backend.knowledge.service.KnowledgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bmcai.backend.embedding.service.KnowledgeEmbeddingIndexService;
import com.bmcai.backend.embedding.service.SemanticSearchService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final KnowledgeService knowledgeService;
    private final EmbeddingChunkService embeddingChunkService;
    private final KnowledgeEmbeddingIndexService knowledgeEmbeddingIndexService;

    private final SemanticSearchService semanticSearchService;

    public EmbeddingController(
            EmbeddingService embeddingService,
            KnowledgeService knowledgeService,
            EmbeddingChunkService embeddingChunkService,
            KnowledgeEmbeddingIndexService
                    knowledgeEmbeddingIndexService,
            SemanticSearchService semanticSearchService
    ) {
        this.embeddingService = embeddingService;
        this.knowledgeService = knowledgeService;
        this.embeddingChunkService =
                embeddingChunkService;
        this.knowledgeEmbeddingIndexService =
                knowledgeEmbeddingIndexService;
        this.semanticSearchService =
                semanticSearchService;
    }

    @GetMapping("/test")
    public Map<String, Object> test(
            @RequestParam String text
    ) {

        float[] embedding =
                embeddingService.generateEmbedding(text);

        return Map.of(
                "text", text,
                "dimensions", embedding.length,
                "embedding", embedding
        );
    }

    @GetMapping("/similarity")
    public Map<String, Object> similarity(
            @RequestParam String textA,
            @RequestParam String textB
    ) {

        float[] embeddingA =
                embeddingService.generateQueryEmbedding(textA);

        float[] embeddingB =
                embeddingService.generatePassageEmbedding(textB);

        double similarity =
                embeddingService.cosineSimilarity(
                        embeddingA,
                        embeddingB
                );

        return Map.of(
                "query", textA,
                "passage", textB,
                "similarity", similarity
        );
    }

    @GetMapping("/document")
    public Map<String, Object> embedDocument(
            @RequestParam String path
    ) {

        List<KnowledgeChunk> chunks =
                knowledgeService.chunkDocument(path);

        List<ChunkEmbedding> embeddings =
                embeddingChunkService.createEmbeddings(
                        chunks
                );

        return Map.of(
                "document", path,
                "chunks", embeddings.size(),
                "dimensions",
                embeddings.isEmpty()
                        ? 0
                        : embeddings.get(0)
                                .getEmbedding()
                                .length,
                "embeddings", embeddings
        );
    }

    @GetMapping("/index")
    public Map<String, Object> buildIndex() {

        int totalChunks =
                knowledgeEmbeddingIndexService
                        .buildIndex();

        return Map.of(
                "status", "ok",
                "indexedChunks", totalChunks
        );
    }

    @GetMapping("/index/status")
    public Map<String, Object> indexStatus() {

        return Map.of(
                "indexedChunks",
                knowledgeEmbeddingIndexService
                        .getIndexedChunks()
        );
    }
}