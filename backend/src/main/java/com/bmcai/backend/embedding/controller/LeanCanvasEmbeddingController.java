package com.bmcai.backend.embedding.controller;

import com.bmcai.backend.embedding.service.LeanCanvasEmbeddingIndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/embedding/lean-canvas")
public class LeanCanvasEmbeddingController {

    private final LeanCanvasEmbeddingIndexService indexService;

    public LeanCanvasEmbeddingController(
            LeanCanvasEmbeddingIndexService indexService
    ) {
        this.indexService = indexService;
    }

    @GetMapping("/index")
    public Map<String, Object> buildIndex() {

        int totalChunks =
                indexService.buildIndex();

        return Map.of(
                "status", "ok",
                "indexedChunks", totalChunks
        );
    }

    @GetMapping("/index/status")
    public Map<String, Object> indexStatus() {

        return Map.of(
                "indexedChunks",
                indexService.getIndexedChunks()
        );
    }

    @GetMapping("/index/file")
    public Map<String, Object> indexFile() {

        return Map.of(
                "exists",
                indexService.indexExists(),
                "path",
                indexService.getIndexPath()
        );
    }

    @GetMapping("/index/load")
    public Map<String, Object> loadIndex() {

        boolean loaded =
                indexService.loadIndex();

        return Map.of(
                "status",
                loaded ? "ok" : "not_found",
                "loaded",
                loaded,
                "indexedChunks",
                indexService.getIndexedChunks()
        );
    }


}
