package com.marykatekitchen.mykitchen_agent.controller;

import com.marykatekitchen.mykitchen_agent.dto.GroceryListResponse;
import com.marykatekitchen.mykitchen_agent.dto.RecipeMatch;
import com.marykatekitchen.mykitchen_agent.model.Recipe;
import com.marykatekitchen.mykitchen_agent.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public Recipe getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }

    @GetMapping("/matches")
    public List<RecipeMatch> getRecipeMatches() {
        return recipeService.getRecipeMatches();
    }

    @GetMapping("/{id}/grocery-list")
    public GroceryListResponse getGroceryList(@PathVariable Long id) {
        return recipeService.getGroceryList(id);
    }

    @PostMapping
    public Recipe addRecipe(@RequestBody Recipe recipe) {
        return recipeService.addRecipe(recipe);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipe(
            @PathVariable Long id,
            @RequestBody Recipe recipe) {

        return recipeService.updateRecipe(id, recipe);
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }
}