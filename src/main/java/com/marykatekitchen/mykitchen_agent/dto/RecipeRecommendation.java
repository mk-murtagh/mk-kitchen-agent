package com.marykatekitchen.mykitchen_agent.dto;

import java.util.List;

public class RecipeRecommendation {

    private Long recipeId;
    private String recipeName;
    private double matchPercentage;
    private double recommendationScore;
    private List<String> expiringIngredients;
    private List<String> missingIngredients;

    public RecipeRecommendation(
            Long recipeId,
            String recipeName,
            double matchPercentage,
            double recommendationScore,
            List<String> expiringIngredients,
            List<String> missingIngredients) {

        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.matchPercentage = matchPercentage;
        this.recommendationScore = recommendationScore;
        this.expiringIngredients = expiringIngredients;
        this.missingIngredients = missingIngredients;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public double getRecommendationScore() {
        return recommendationScore;
    }

    public List<String> getExpiringIngredients() {
        return expiringIngredients;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }
}