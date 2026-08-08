package com.marykatekitchen.mykitchen_agent.dto;

import java.util.List;

public class GroceryListResponse {

    private Long recipeId;
    private String recipeName;
    private List<GroceryItem> items;

    public GroceryListResponse(
            Long recipeId,
            String recipeName,
            List<GroceryItem> items) {

        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.items = items;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public List<GroceryItem> getItems() {
        return items;
    }
}