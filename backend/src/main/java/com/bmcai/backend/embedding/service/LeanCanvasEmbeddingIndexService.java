package com.bmcai.backend.embedding.service;

import com.bmcai.backend.embedding.model.ChunkEmbedding;
import com.bmcai.backend.knowledge.model.KnowledgeChunk;
import com.bmcai.backend.knowledge.service.KnowledgeChunkService;
import com.bmcai.backend.knowledge.service.KnowledgeReaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class LeanCanvasEmbeddingIndexService {


    private final EmbeddingChunkService embeddingChunkService;
    private final KnowledgeReaderService knowledgeReaderService;
    private final KnowledgeChunkService knowledgeChunkService;
    private final LeanCanvasVectorStoreService vectorStoreService;

    private final Path leanCanvasPath;

    public LeanCanvasEmbeddingIndexService(
            EmbeddingChunkService embeddingChunkService,
            KnowledgeReaderService knowledgeReaderService,
            KnowledgeChunkService knowledgeChunkService,
            LeanCanvasVectorStoreService vectorStoreService,
            @Value("${knowledge.path}") String knowledgePath
    ) {
        this.embeddingChunkService = embeddingChunkService;
        this.knowledgeReaderService = knowledgeReaderService;
        this.knowledgeChunkService = knowledgeChunkService;
        this.vectorStoreService = vectorStoreService;

        this.leanCanvasPath = Path.of(knowledgePath)
                .resolve("lean-canvas")
                .toAbsolutePath()
                .normalize();
    }

    public int buildIndex() {

        if (!Files.exists(leanCanvasPath)) {
            throw new IllegalStateException(
                    "La carpeta de conocimiento de Lean Canvas no existe: "
                            + leanCanvasPath
            );
        }

        List<ChunkEmbedding> allEmbeddings =
                new ArrayList<>();

        try (Stream<Path> paths = Files.walk(leanCanvasPath)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(this::isMarkdown)
                    .forEach(path -> {

                        String content =
                                knowledgeReaderService.readMarkdown(path);

                        List<KnowledgeChunk> chunks =
                                knowledgeChunkService.createChunks(
                                        path.getFileName().toString(),
                                        leanCanvasPath
                                                .relativize(path)
                                                .toString(),
                                        "markdown",
                                        content
                                );

                        allEmbeddings.addAll(
                                embeddingChunkService.createEmbeddings(
                                        chunks
                                )
                        );
                    });

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al construir el índice de Lean Canvas",
                    e
            );
        }

        vectorStoreService.clear();
        vectorStoreService.addAll(allEmbeddings);
        vectorStoreService.save();

        return vectorStoreService.size();
    }

    public int getIndexedChunks() {
        return vectorStoreService.size();
    }

    public boolean indexExists() {
        return vectorStoreService.exists();
    }

    public String getIndexPath() {
        return vectorStoreService.getIndexPath();
    }

    public boolean loadIndex() {
        return vectorStoreService.load();
    }

    private boolean isMarkdown(Path path) {

        return path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(".md");
    }


}
