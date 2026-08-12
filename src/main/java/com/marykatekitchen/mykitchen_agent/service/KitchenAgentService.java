package com.marykatekitchen.mykitchen_agent.service;

import com.marykatekitchen.mykitchen_agent.dto.RecipeRecommendation;
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
    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;
    private final String deploymentName;

    public KitchenAgentService(
            RestClient azureOpenAIRestClient,
            IngredientService ingredientService,
            RecipeService recipeService,
            ObjectMapper objectMapper,
            @Value("${AZURE_OPENAI_DEPLOYMENT}") String deploymentName) {

        this.restClient = azureOpenAIRestClient;
        this.ingredientService = ingredientService;
        this.recipeService = recipeService;
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

    private Map<String, Object> getRecipeRecommendationsToolDefinition() {
        return Map.of(
                "type", "function",
                "name", "getRecipeRecommendations",
                "description",
                "Get recipe recommendations based on the user's pantry inventory and ingredients that expire within a specified number of days.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "days", Map.of(
                                        "type", "integer",
                                        "description",
                                        "Number of days ahead to consider ingredients as expiring soon."
                                )
                        ),
                        "required", List.of("days"),
                        "additionalProperties", false
                )
        );
    }

    private List<Ingredient> getPantry() {
        return ingredientService.getAllIngredients();
    }

    private List<RecipeRecommendation> getRecipeRecommendations(int days) {
        return recipeService.getRecipeRecommendations(days);
    }

    public String chat(String message) {

        String instructions = """
            You are a kitchen assistant connected to the user's real pantry
            and recipe database.

            Use getPantry whenever the user asks about:
            - pantry contents
            - ingredient quantities
            - storage locations
            - expiration dates

            Use getRecipeRecommendations when the user asks:
            - what they should cook
            - what recipes they can make
            - what they can make with ingredients expiring soon
            - for meal recommendations based on their pantry

            If the user does not specify an expiration window,
            use 7 days.

            Important rules:
            - Never guess pantry contents.
            - Never invent recipes that were not returned by a tool.
            - Never invent recipe ingredients, quantities, instructions,
            cooking times, or servings that were not returned by a tool.
            - Clearly distinguish ingredients the user has from ingredients
            they are missing.
            Important:
            - Only use recipe ingredients, quantities, servings, and instructions that were returned by a tool.
            - Do not add common recipe ingredients from general knowledge.
            - Do not generate cooking instructions unless they exist in the recipe data returned by a tool.
            - When information is unavailable, say that it is unavailable instead of filling it in.
            - Use tools whenever a question depends on pantry or recipe data.
            - If another available tool can answer the user's question,
            call it instead of asking the user whether you should call it.
            """;

        Map<String, Object> requestBody = Map.of(
                "model", deploymentName,
                "instructions", instructions,
                "input", message,
                "tools", List.of(
                            getPantryToolDefinition(),
                            getRecipeRecommendationsToolDefinition()
                        )
        );

        Map<?, ?> response = restClient.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return processResponse(response);
    }

    private int extractDays(Map<?, ?> outputItem) {

        Object argumentsObject = outputItem.get("arguments");

        if (!(argumentsObject instanceof String argumentsJson)) {
            return 7;
        }

        try {
            Map<?, ?> arguments =
                    objectMapper.readValue(argumentsJson, Map.class);

            Object daysObject =
                    arguments.get("days");

            if (daysObject instanceof Number number) {
                return number.intValue();
            }

        } catch (Exception e) {
            return 7;
        }

        return 7;
    }

    private String sendToolResultToModel(
            Map<?, ?> originalResponse,
            String callId,
            Object toolResult) {

        String previousResponseId =
                (String) originalResponse.get("id");

        String resultJson;

        try {
            resultJson = objectMapper.writeValueAsString(toolResult);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize tool result", e);
        }

        Map<String, Object> toolOutput = Map.of(
                "type", "function_call_output",
                "call_id", callId,
                "output", resultJson
        );

        Map<String, Object> requestBody = Map.of(
                "model", deploymentName,
                "previous_response_id", previousResponseId,
                "input", List.of(toolOutput),
                "tools", List.of(
                        getPantryToolDefinition(),
                        getRecipeRecommendationsToolDefinition()
                )
        );

        Map<?, ?> finalResponse = restClient.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (finalResponse == null) {
            return "No response returned.";
        }

        return processResponse(finalResponse);
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

    private String processResponse(Map<?, ?> response) {
        if (response == null) {
            return "No response returned.";
        }

        List<?> output = (List<?>) response.get("output");

        if (output == null || output.isEmpty()) {
            return "No response returned.";
        }

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

            if ("getRecipeRecommendations".equals(functionName)) {

                int days = extractDays(outputItem);

                List<RecipeRecommendation> recommendations =
                        getRecipeRecommendations(days);

                return sendToolResultToModel(
                        response,
                        callId,
                        recommendations
                );
            }
        }

        return extractText(response);
    }
}