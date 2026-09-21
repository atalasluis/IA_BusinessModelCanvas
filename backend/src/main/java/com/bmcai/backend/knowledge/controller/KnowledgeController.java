package com.bmcai.backend.knowledge.controller;

import com.bmcai.backend.knowledge.model.KnowledgeContent;
import com.bmcai.backend.knowledge.model.KnowledgeDocument;
import com.bmcai.backend.knowledge.service.KnowledgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bmcai.backend.knowledge.model.KnowledgeChunk;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/documents")
    public List<KnowledgeDocument> listDocuments() {
        return knowledgeService.listDocuments();
    }

    @GetMapping("/document")
    public KnowledgeContent readDocument(
            @RequestParam String path
    ) {
        return knowledgeService.readDocument(path);
    }

    @GetMapping("/chunks")
    public List<KnowledgeChunk> getChunks(
            @RequestParam String path
    ) {
        return knowledgeService.chunkDocument(path);
    }
}