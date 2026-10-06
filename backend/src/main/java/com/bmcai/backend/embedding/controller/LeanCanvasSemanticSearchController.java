package com.bmcai.backend.embedding.controller;

import com.bmcai.backend.embedding.model.SearchResult;
import com.bmcai.backend.embedding.service.LeanCanvasSemanticSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/embedding/lean-canvas")
public class LeanCanvasSemanticSearchController {


    private final LeanCanvasSemanticSearchService searchService;

    public LeanCanvasSemanticSearchController(
            LeanCanvasSemanticSearchService searchService
    ) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<SearchResult> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK
    ) {

        return searchService.search(
                query,
                topK
        );
    }


}
