package com.bmcai.backend.knowledge.service;

import com.bmcai.backend.knowledge.model.KnowledgeChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeChunkService {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 150;

    public List<KnowledgeChunk> createChunks(
            String documentName,
            String documentPath,
            String documentType,
            String content
    ) {

        List<KnowledgeChunk> chunks = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return chunks;
        }

        String normalizedContent = content
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();

        int start = 0;
        int chunkIndex = 0;

        while (start < normalizedContent.length()) {

            int end = Math.min(
                    start + CHUNK_SIZE,
                    normalizedContent.length()
            );

            if (end < normalizedContent.length()) {

                int lastNewLine = normalizedContent.lastIndexOf(
                        '\n',
                        end
                );

                if (lastNewLine > start) {
                    end = lastNewLine;
                }
            }

            String chunkContent = normalizedContent
                    .substring(start, end)
                    .trim();

            if (!chunkContent.isBlank()) {

                chunks.add(
                        new KnowledgeChunk(
                                documentName,
                                documentPath,
                                documentType,
                                chunkIndex,
                                chunkContent
                        )
                );

                chunkIndex++;
            }

            if (end >= normalizedContent.length()) {
                break;
            }

            start = Math.max(
                    end - CHUNK_OVERLAP,
                    start + 1
            );
        }

        return chunks;
    }
}