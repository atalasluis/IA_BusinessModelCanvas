package com.bmcai.backend.knowledge.service;

import com.bmcai.backend.knowledge.model.KnowledgeChunk;
import com.bmcai.backend.knowledge.model.KnowledgeContent;
import com.bmcai.backend.knowledge.model.KnowledgeDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class KnowledgeService {

    private final Path knowledgePath;
    private final KnowledgeReaderService knowledgeReaderService;
    private final KnowledgeChunkService knowledgeChunkService;

    public KnowledgeService(
            @Value("${knowledge.path}") String knowledgePath,
            KnowledgeReaderService knowledgeReaderService,
            KnowledgeChunkService knowledgeChunkService
    ) {
        this.knowledgePath = Path.of(knowledgePath)
                .toAbsolutePath()
                .normalize();

        this.knowledgeReaderService = knowledgeReaderService;
        this.knowledgeChunkService = knowledgeChunkService;
    }

    public List<KnowledgeDocument> listDocuments() {

        List<KnowledgeDocument> documents = new ArrayList<>();

        if (!Files.exists(knowledgePath)) {
            return documents;
        }

        try (Stream<Path> paths = Files.walk(knowledgePath)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .forEach(path -> {

                        String type = getFileType(path);

                        documents.add(
                                new KnowledgeDocument(
                                        path.getFileName().toString(),
                                        knowledgePath
                                                .relativize(path)
                                                .toString(),
                                        type,
                                        getFileSize(path)
                                )
                        );
                    });

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al leer la carpeta de conocimiento",
                    e
            );
        }

        return documents;
    }

    public KnowledgeContent readDocument(String relativePath) {

        Path documentPath = knowledgePath
                .resolve(relativePath)
                .normalize();

        if (!documentPath.startsWith(knowledgePath)) {
            throw new IllegalArgumentException(
                    "La ruta del documento no es válida"
            );
        }

        if (!Files.exists(documentPath)) {
            throw new IllegalArgumentException(
                    "El documento no existe: " + relativePath
            );
        }

        if (!Files.isRegularFile(documentPath)) {
            throw new IllegalArgumentException(
                    "La ruta no corresponde a un archivo"
            );
        }

        if (!isSupportedFile(documentPath)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no soportado"
            );
        }

        String content = knowledgeReaderService.read(documentPath);

        return new KnowledgeContent(
                documentPath.getFileName().toString(),
                knowledgePath
                        .relativize(documentPath)
                        .toString(),
                getFileType(documentPath),
                content,
                getFileSize(documentPath)
        );
    }

    public List<KnowledgeChunk> chunkDocument(String relativePath) {

        KnowledgeContent document = readDocument(relativePath);

        return knowledgeChunkService.createChunks(
                document.getName(),
                document.getPath(),
                document.getType(),
                document.getContent()
        );
    }

    private boolean isSupportedFile(Path path) {

        String fileName = path.getFileName()
                .toString()
                .toLowerCase();

        return fileName.endsWith(".md")
                || fileName.endsWith(".pdf");
    }

    private String getFileType(Path path) {

        String fileName = path.getFileName()
                .toString()
                .toLowerCase();

        if (fileName.endsWith(".md")) {
            return "markdown";
        }

        if (fileName.endsWith(".pdf")) {
            return "pdf";
        }

        return "unknown";
    }

    private long getFileSize(Path path) {

        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }
}