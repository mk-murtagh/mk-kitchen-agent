package com.marykatekitchen.mykitchen_agent.service;

import com.marykatekitchen.mykitchen_agent.dto.GroceryListResponse;
import com.marykatekitchen.mykitchen_agent.dto.RecipeMatch;
import com.marykatekitchen.mykitchen_agent.dto.RecipeRecommendation;
import com.marykatekitchen.mykitchen_agent.model.Ingredient;
import com.marykatekitchen.mykitchen_agent.model.Recipe;
import com.marykatekitchen.mykitchen_agent.model.RecipeIngredient;
import com.marykatekitchen.mykitchen_agent.repository.IngredientRepository;
import com.marykatekitchen.mykitchen_agent.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void addRecipe_assignsRecipeToEachIngredientAndSavesIt() {
        Recipe recipe = new Recipe();
        recipe.setName("Pasta");
        recipe.setInstructions("Boil pasta");

        RecipeIngredient ingredientOne = new RecipeIngredient("Pasta", 200.0, "g");
        RecipeIngredient ingredientTwo = new RecipeIngredient("Sauce", 100.0, "ml");
        recipe.setIngredients(List.of(ingredientOne, ingredientTwo));

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe savedRecipe = recipeService.addRecipe(recipe);

        assertSame(recipe, savedRecipe);
        assertSame(recipe, ingredientOne.getRecipe());
        assertSame(recipe, ingredientTwo.getRecipe());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void updateRecipe_replacesIngredientsAndPersistsChanges() {
        Recipe existingRecipe = new Recipe();
        existingRecipe.setName("Old recipe");
        existingRecipe.setServings(2);
        existingRecipe.setInstructions("Old instructions");
        existingRecipe.setIngredients(List.of(new RecipeIngredient("Salt", 1.0, "g")));

        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setName("New recipe");
        updatedRecipe.setServings(4);
        updatedRecipe.setInstructions("New instructions");
        updatedRecipe.setIngredients(List.of(new RecipeIngredient("Sugar", 2.0, "g")));

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existingRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe savedRecipe = recipeService.updateRecipe(1L, updatedRecipe);

        assertEquals("New recipe", savedRecipe.getName());
        assertEquals(4, savedRecipe.getServings());
        assertEquals("New instructions", savedRecipe.getInstructions());
        assertEquals(1, savedRecipe.getIngredients().size());
        assertEquals("Sugar", savedRecipe.getIngredients().get(0).getName());
        assertSame(savedRecipe, savedRecipe.getIngredients().get(0).getRecipe());
        verify(recipeRepository).save(existingRecipe);
    }

    @Test
    void getRecipeMatches_returnsSortedMatchesBasedOnPantryInventory() {
        Recipe recipeOne = new Recipe();
        recipeOne.setName("Soup");
        recipeOne.setIngredients(List.of(
                new RecipeIngredient("Tomato", 2.0, "kg"),
                new RecipeIngredient("Salt", 1.0, "g")
        ));

        Recipe recipeTwo = new Recipe();
        recipeTwo.setName("Salad");
        recipeTwo.setIngredients(List.of(new RecipeIngredient("Lettuce", 1.0, "head")));

        Ingredient pantryTomato = new Ingredient("Tomato", 2.0, "kg", "Fridge", null);
        Ingredient pantryLettuce = new Ingredient("Lettuce", 1.0, "head", "Fridge", null);

        when(recipeRepository.findAll()).thenReturn(List.of(recipeOne, recipeTwo));
        when(ingredientRepository.findAll()).thenReturn(List.of(pantryTomato, pantryLettuce));

        List<RecipeMatch> matches = recipeService.getRecipeMatches();

        assertEquals(2, matches.size());
        assertEquals("Salad", matches.get(0).getRecipeName());
        assertEquals(100.0, matches.get(0).getMatchPercentage());
        assertEquals("Soup", matches.get(1).getRecipeName());
        assertEquals(50.0, matches.get(1).getMatchPercentage());
        assertTrue(matches.get(1).getOwnedIngredients().contains("Tomato"));
        assertTrue(matches.get(1).getMissingIngredients().contains("Salt"));
    }

    @Test
    void getGroceryList_returnsOnlyItemsThatNeedToBeBought() {
        Recipe recipe = new Recipe();
        recipe.setId(7L);
        recipe.setName("Breakfast");
        recipe.setIngredients(List.of(
                new RecipeIngredient("Eggs", 4.0, "pieces"),
                new RecipeIngredient("Milk", 2.0, "L")
        ));

        Ingredient pantryEggs = new Ingredient("Eggs", 2.0, "pieces", "Fridge", null);

        when(recipeRepository.findById(7L)).thenReturn(Optional.of(recipe));
        when(ingredientRepository.findAll()).thenReturn(List.of(pantryEggs));

        GroceryListResponse response = recipeService.getGroceryList(7L);

        assertEquals(7L, response.getRecipeId());
        assertEquals("Breakfast", response.getRecipeName());
        assertEquals(2, response.getItems().size());
        assertEquals("Eggs", response.getItems().get(0).getName());
        assertEquals(4.0, response.getItems().get(0).getRequiredQuantity());
        assertEquals(2.0, response.getItems().get(0).getAvailableQuantity());
        assertEquals(2.0, response.getItems().get(0).getMissingQuantity());
        assertEquals("Milk", response.getItems().get(1).getName());
    }

    @Test
    void getRecipeRecommendations_prioritizesExpiringIngredientsAndMatchRate() {
        Recipe recipe = new Recipe();
        recipe.setName("Dinner");
        recipe.setIngredients(List.of(
                new RecipeIngredient("Beans", 2.0, "cans"),
                new RecipeIngredient("Rice", 1.0, "cup")
        ));

        Ingredient pantryBeans = new Ingredient("Beans", 1.0, "cans", "Pantry", LocalDate.now().plusDays(2));
        Ingredient pantryRice = new Ingredient("Rice", 1.0, "cup", "Pantry", null);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe));
        when(ingredientRepository.findAll()).thenReturn(List.of(pantryBeans, pantryRice));

        List<RecipeRecommendation> recommendations = recipeService.getRecipeRecommendations(3);

        assertEquals(1, recommendations.size());
        assertEquals("Dinner", recommendations.get(0).getRecipeName());
        assertEquals(75.0, recommendations.get(0).getMatchPercentage());
        assertEquals(85.0, recommendations.get(0).getRecommendationScore());
        assertTrue(recommendations.get(0).getExpiringIngredients().contains("Beans"));
        assertTrue(recommendations.get(0).getMissingIngredients().contains("Beans"));
    }

    @Test
    void cookRecipe_reducesPantryQuantityAndPersistsTheChange() {
        Recipe recipe = new Recipe();
        recipe.setName("Bread");
        recipe.setIngredients(List.of(new RecipeIngredient("Flour", 2.0, "kg")));

        Ingredient pantryFlour = new Ingredient("Flour", 5.0, "kg", "Pantry", null);

        when(recipeRepository.findById(3L)).thenReturn(Optional.of(recipe));
        when(ingredientRepository.findAll()).thenReturn(List.of(pantryFlour));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recipeService.cookRecipe(3L);

        assertEquals(3.0, pantryFlour.getQuantity());
        verify(ingredientRepository).save(pantryFlour);
    }
}
