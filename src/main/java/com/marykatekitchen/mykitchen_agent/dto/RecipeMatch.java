package com.marykatekitchen.mykitchen_agent.dto;

import java.util.List;

public class RecipeMatch {

    private Long recipeId;
    private String recipeName;
    private double matchPercentage;
    private List<String> ownedIngredients;
    private List<String> missingIngredients;

    public RecipeMatch(
            Long recipeId,
            String recipeName,
            double matchPercentage,
            List<String> ownedIngredients,
            List<String> missingIngredients) {

        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.matchPercentage = matchPercentage;
        this.ownedIngredients = ownedIngredients;
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

    public List<String> getOwnedIngredients() {
        return ownedIngredients;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }
}