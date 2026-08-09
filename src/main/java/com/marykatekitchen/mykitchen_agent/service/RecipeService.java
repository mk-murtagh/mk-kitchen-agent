package com.marykatekitchen.mykitchen_agent.service;

import com.marykatekitchen.mykitchen_agent.dto.GroceryItem;
import com.marykatekitchen.mykitchen_agent.dto.GroceryListResponse;
import com.marykatekitchen.mykitchen_agent.dto.RecipeMatch;
import com.marykatekitchen.mykitchen_agent.model.Ingredient;
import com.marykatekitchen.mykitchen_agent.model.Recipe;
import com.marykatekitchen.mykitchen_agent.model.RecipeIngredient;
import com.marykatekitchen.mykitchen_agent.repository.IngredientRepository;
import com.marykatekitchen.mykitchen_agent.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public Recipe addRecipe(Recipe recipe) {

        if (recipe.getIngredients() != null) {
            recipe.getIngredients().forEach(
                    ingredient -> ingredient.setRecipe(recipe)
            );
        }

        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(Long id, Recipe updatedRecipe) {
        Recipe recipe = getRecipeById(id);

        recipe.setName(updatedRecipe.getName());
        recipe.setServings(updatedRecipe.getServings());
        recipe.setInstructions(updatedRecipe.getInstructions());

        if (updatedRecipe.getIngredients() != null) {
            updatedRecipe.getIngredients().forEach(
                    ingredient -> ingredient.setRecipe(recipe)
            );
        }

        recipe.setIngredients(updatedRecipe.getIngredients());

        return recipeRepository.save(recipe);
    }

    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    public List<RecipeMatch> getRecipeMatches() {
        List<Recipe> recipes = recipeRepository.findAll();
        List<Ingredient> pantry = ingredientRepository.findAll();

        List<RecipeMatch> matches = new ArrayList<>();

        for (Recipe recipe : recipes) {

            List<String> owned = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            double totalScore = 0.0;

            for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {

                Ingredient pantryIngredient = pantry.stream()
                        .filter(ingredient ->
                                ingredient.getName()
                                        .equalsIgnoreCase(recipeIngredient.getName())
                        )
                        .filter(ingredient ->
                                ingredient.getUnit()
                                        .equalsIgnoreCase(recipeIngredient.getUnit())
                        )
                        .findFirst()
                        .orElse(null);

                double requiredQuantity = recipeIngredient.getQuantity();

                double availableQuantity =
                        pantryIngredient == null
                                ? 0.0
                                : pantryIngredient.getQuantity();

                double ingredientScore =
                        Math.min(availableQuantity / requiredQuantity, 1.0);

                totalScore += ingredientScore;

                if (availableQuantity >= requiredQuantity) {
                    owned.add(recipeIngredient.getName());
                } else {
                    missing.add(recipeIngredient.getName());
                }
            }

            int totalIngredients = recipe.getIngredients().size();

            double matchPercentage =
                    totalIngredients == 0
                            ? 0.0
                            : (totalScore / totalIngredients) * 100;

            matches.add(
                    new RecipeMatch(
                            recipe.getId(),
                            recipe.getName(),
                            matchPercentage,
                            owned,
                            missing
                    )
            );
        }

        return matches;
    }

    public GroceryListResponse getGroceryList(Long recipeId) {

        Recipe recipe = getRecipeById(recipeId);
        List<Ingredient> pantry = ingredientRepository.findAll();

        List<GroceryItem> missingItems = new ArrayList<>();

        for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {

            Ingredient pantryIngredient = pantry.stream()
                .filter(ingredient ->
                        ingredient.getName()
                                .equalsIgnoreCase(recipeIngredient.getName())
                )
                .filter(ingredient ->
                        ingredient.getUnit()
                                .equalsIgnoreCase(recipeIngredient.getUnit())
                )
                .findFirst()
                .orElse(null);

            double requiredQuantity = recipeIngredient.getQuantity();

            double availableQuantity =
                    pantryIngredient == null
                            ? 0
                            : pantryIngredient.getQuantity();

            if (availableQuantity < requiredQuantity) {

                double missingQuantity =
                        requiredQuantity - availableQuantity;

                missingItems.add(
                        new GroceryItem(
                                recipeIngredient.getName(),
                                requiredQuantity,
                                availableQuantity,
                                missingQuantity,
                                recipeIngredient.getUnit()
                        )
                );
            }
        }

        return new GroceryListResponse(
                recipe.getId(),
                recipe.getName(),
                missingItems
        );
    }
}
