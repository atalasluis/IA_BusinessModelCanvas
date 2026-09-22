package com.bmcai.backend.bmc.controller;

import com.bmcai.backend.bmc.model.BmcContextRequest;
import com.bmcai.backend.bmc.model.BmcContextResponse;
import com.bmcai.backend.bmc.service.BmcContextService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bmc")
public class BmcContextController {

    private final BmcContextService bmcContextService;

    public BmcContextController(
            BmcContextService bmcContextService
    ) {
        this.bmcContextService =
                bmcContextService;
    }

    @PostMapping("/context")
    public BmcContextResponse buildContext(
            @RequestBody BmcContextRequest request
    ) {

        String prompt =
                bmcContextService.buildPrompt(
                        request.getContext(),
                        request.getParameters()
                );

        return new BmcContextResponse(prompt);
    }
}