package com.marykatekitchen.mykitchen_agent.service;

import com.marykatekitchen.mykitchen_agent.model.Ingredient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

@Service
public class KitchenAgentService {

    private final RestClient restClient;
    private final IngredientService ingredientService;
    private final ObjectMapper objectMapper;
    private final String deploymentName;

    public KitchenAgentService(
            RestClient azureOpenAIRestClient,
            IngredientService ingredientService,
            ObjectMapper objectMapper,
            @Value("${AZURE_OPENAI_DEPLOYMENT}") String deploymentName) {

        this.restClient = azureOpenAIRestClient;
        this.ingredientService = ingredientService;
        this.objectMapper = objectMapper;
        this.deploymentName = deploymentName;
    }

    private Map<String, Object> getPantryToolDefinition() {
        return Map.of(
                "type", "function",
                "name", "getPantry",
                "description",
                "Get the user's current pantry ingredients, including quantity, unit, location, and expiration date.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "additionalProperties", false
                )
        );
    }

    private List<Ingredient> getPantry() {
        return ingredientService.getAllIngredients();
    }

    public String chat(String message) {

        Map<String, Object> requestBody = Map.of(
                "model", deploymentName,
                "instructions",
                """
                You are a kitchen assistant.

                Use the getPantry tool whenever the user asks about:
                - what ingredients they currently have
                - ingredient quantities
                - where ingredients are stored
                - expiration dates

                Never guess pantry contents. Use getPantry for pantry-specific facts.
                """,
                "input", message,
                "tools", List.of(getPantryToolDefinition())
        );

        Map<?, ?> response = restClient.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return "No response returned.";
        }

        List<?> output = (List<?>) response.get("output");

        if (output == null || output.isEmpty()) {
            return "No response returned.";
        }

        /*
        * Look through Azure's response to see whether
        * the model requested getPantry.
        */
        for (Object item : output) {

            if (!(item instanceof Map<?, ?> outputItem)) {
                continue;
            }

            if (!"function_call".equals(outputItem.get("type"))) {
                continue;
            }

            String functionName =
                    (String) outputItem.get("name");

            String callId =
                    (String) outputItem.get("call_id");

            if ("getPantry".equals(functionName)) {

                List<Ingredient> pantry =
                        getPantry();

                return sendToolResultToModel(
                        response,
                        callId,
                        pantry
                );
            }
        }

        /*
        * If the model did not call a tool,
        * just return its normal text response.
        */
        return extractText(response);
    }

    private String sendToolResultToModel(
            Map<?, ?> originalResponse,
            String callId,
            List<Ingredient> pantry) {

        String previousResponseId =
                (String) originalResponse.get("id");

        String pantryJson;

        try {
            pantryJson = objectMapper.writeValueAsString(pantry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize pantry data", e);
        }

        Map<String, Object> toolOutput = Map.of(
                "type", "function_call_output",
                "call_id", callId,
                "output", pantryJson
        );

        Map<String, Object> requestBody = Map.of(
                "model", deploymentName,
                "previous_response_id", previousResponseId,
                "input", List.of(toolOutput)
        );

        Map<?, ?> finalResponse = restClient.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (finalResponse == null) {
            return "No response returned.";
        }

        return extractText(finalResponse);
    }

    private String extractText(Map<?, ?> response) {

        List<?> output =
                (List<?>) response.get("output");

        if (output == null) {
            return "No response returned.";
        }

        for (Object item : output) {

            if (!(item instanceof Map<?, ?> outputItem)) {
                continue;
            }

            Object contentObject =
                    outputItem.get("content");

            if (!(contentObject instanceof List<?> content)) {
                continue;
            }

            for (Object contentItem : content) {

                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object text =
                        contentMap.get("text");

                if (text instanceof String result) {
                    return result;
                }
            }
        }

        return "No response returned.";
    }
}