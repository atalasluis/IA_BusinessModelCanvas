package com.bmcai.backend.generator.controller;

import com.bmcai.backend.generator.model.GenerateRequest;
import com.bmcai.backend.generator.model.GenerateResponse;
import com.bmcai.backend.generator.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generate")
@CrossOrigin(origins = "*")
public class GeneratorController {

    private final GeminiService geminiService;

    public GeneratorController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public GenerateResponse generateCanvas(@RequestBody GenerateRequest request) throws Exception {
        return geminiService.callGemini(request);
    }
}
