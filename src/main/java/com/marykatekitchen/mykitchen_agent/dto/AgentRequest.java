package com.marykatekitchen.mykitchen_agent.dto;

import jakarta.validation.constraints.NotBlank;

public class AgentRequest {

    @NotBlank(message = "message is required")
    private String message;

    public AgentRequest() {
    }

    public AgentRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}