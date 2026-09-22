package com.bmcai.backend.rag.controller;

import com.bmcai.backend.rag.service.RagService;
import com.bmcai.backend.rag.service.RagService.RagResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(
            RagService ragService
    ) {
        this.ragService = ragService;
    }

    @GetMapping("/context")
    public Map<String, Object> getContext(
            @RequestParam String query,
            @RequestParam(required = false)
            Integer topK
    ) {

        RagResponse response;

        if (topK == null) {
            response =
                    ragService.retrieve(query);
        } else {
            response =
                    ragService.retrieve(
                            query,
                            topK
                    );
        }

        return Map.of(
                "query", response.getQuery(),
                "topK", response.getTopK(),
                "retrievedResults",
                response.getResults().size(),
                "relevantResults",
                response.getRelevantResults(),
                "relevant",
                response.isRelevant(),
                "context",
                response.getContext()
        );
    }
}