package com.marykatekitchen.mykitchen_agent.controller;

import com.marykatekitchen.mykitchen_agent.dto.AgentRequest;
import com.marykatekitchen.mykitchen_agent.dto.AgentResponse;
import com.marykatekitchen.mykitchen_agent.service.KitchenAgentService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final KitchenAgentService kitchenAgentService;

    public AgentController(KitchenAgentService kitchenAgentService) {
        this.kitchenAgentService = kitchenAgentService;
    }

    @PostMapping("/chat")
    public AgentResponse chat(
            @Valid @RequestBody AgentRequest request) {

        String response =
                kitchenAgentService.chat(request.getMessage());

        return new AgentResponse(response);
    }
}