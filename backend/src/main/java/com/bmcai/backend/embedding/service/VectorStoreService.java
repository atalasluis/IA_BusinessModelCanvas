package com.bmcai.backend.embedding.service;

import com.bmcai.backend.embedding.model.ChunkEmbedding;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class VectorStoreService {

    private final List<ChunkEmbedding> embeddings =
            new ArrayList<>();

    private final ObjectMapper objectMapper;
    private final Path indexPath;

    public VectorStoreService(
            @Value("${embedding.index.path:../models/embeddings.json}")
            String indexPath
    ) {
        this.objectMapper = new ObjectMapper();

        this.indexPath = Path.of(indexPath)
                .toAbsolutePath()
                .normalize();
    }

    public void clear() {
        embeddings.clear();
    }

    public void add(ChunkEmbedding embedding) {

        if (embedding == null) {
            throw new IllegalArgumentException(
                    "El embedding no puede ser null"
            );
        }

        embeddings.add(embedding);
    }

    public void addAll(
            List<ChunkEmbedding> newEmbeddings
    ) {

        if (newEmbeddings == null) {
            return;
        }

        for (ChunkEmbedding embedding : newEmbeddings) {
            add(embedding);
        }
    }

    public List<ChunkEmbedding> getAll() {
        return List.copyOf(embeddings);
    }

    public int size() {
        return embeddings.size();
    }

    public void save() {

        try {
            Path parent = indexPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(
                            indexPath.toFile(),
                            embeddings
                    );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al guardar el índice vectorial",
                    e
            );
        }
    }

    public boolean load() {

        if (!Files.exists(indexPath)) {
            return false;
        }

        try {

            List<ChunkEmbedding> loadedEmbeddings =
                    objectMapper.readValue(
                            indexPath.toFile(),
                            new TypeReference<List<ChunkEmbedding>>() {}
                    );

            embeddings.clear();
            embeddings.addAll(loadedEmbeddings);

            return true;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al cargar el índice vectorial",
                    e
            );
        }
    }

    public boolean exists() {
        return Files.exists(indexPath);
    }

    public String getIndexPath() {
        return indexPath.toString();
    }
}