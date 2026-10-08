package com.bmcai.backend.generator.controller;

import com.bmcai.backend.generator.model.GenerateRequest;
import com.bmcai.backend.generator.model.GenerateResponse;
import com.bmcai.backend.generator.model.GenerateLeanResponse;
import com.bmcai.backend.generator.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class GeneratorController {

    private final GeminiService geminiService;

    public GeneratorController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateCanvas(@RequestBody GenerateRequest request) {
        try {
            GenerateResponse response = geminiService.callGemini(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/generate-lean")
    public ResponseEntity<?> generateLeanCanvas(@RequestBody GenerateRequest request) {
        try {
            GenerateLeanResponse response = geminiService.callGeminiForLean(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/generate-mockup")
    public ResponseEntity<?> generateMockupImage(@RequestBody GenerateRequest request) {
        try {
            // Llama al servicio para que Gemini (o un servicio de imágenes) cree la URL
            String imageUrl = geminiService.generateMockup(request);
            return ResponseEntity.ok(Map.of("image_url", imageUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
