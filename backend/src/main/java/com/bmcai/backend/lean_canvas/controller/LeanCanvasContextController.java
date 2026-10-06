package com.bmcai.backend.lean_canvas.controller;

import com.bmcai.backend.lean_canvas.model.LeanCanvasContextRequest;
import com.bmcai.backend.lean_canvas.model.LeanCanvasContextResponse;
import com.bmcai.backend.lean_canvas.service.LeanCanvasContextService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lean-canvas")
public class LeanCanvasContextController {

    private final LeanCanvasContextService leanCanvasContextService;

    public LeanCanvasContextController(
            LeanCanvasContextService leanCanvasContextService
    ) {
        this.leanCanvasContextService =
                leanCanvasContextService;
    }

    @PostMapping("/context")
    public LeanCanvasContextResponse buildContext(
            @RequestBody LeanCanvasContextRequest request
    ) {

        String prompt =
                leanCanvasContextService.buildPrompt(
                        request.getContext(),
                        request.getParameters()
                );

        return new LeanCanvasContextResponse(prompt);
    }
}