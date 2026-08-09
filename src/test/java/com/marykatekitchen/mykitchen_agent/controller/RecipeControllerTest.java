package com.marykatekitchen.mykitchen_agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marykatekitchen.mykitchen_agent.dto.GroceryListResponse;
import com.marykatekitchen.mykitchen_agent.dto.RecipeMatch;
import com.marykatekitchen.mykitchen_agent.model.Recipe;
import com.marykatekitchen.mykitchen_agent.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new RecipeController(recipeService)).build();
    }

    @Test
    void getRecipeById_returnsOk() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Pasta");

        when(recipeService.getRecipeById(1L)).thenReturn(recipe);

        mockMvc.perform(get("/api/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pasta"));
    }

    @Test
    void getRecipeMatches_returnsOk() throws Exception {
        when(recipeService.getRecipeMatches())
                .thenReturn(List.of(new RecipeMatch(1L, "Pasta", 100.0, List.of("Salt"), List.of())));

        mockMvc.perform(get("/api/recipes/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeName").value("Pasta"));
    }

    @Test
    void getGroceryList_returnsOk() throws Exception {
        when(recipeService.getGroceryList(1L))
                .thenReturn(new GroceryListResponse(1L, "Pasta", List.of()));

        mockMvc.perform(get("/api/recipes/1/grocery-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeName").value("Pasta"));
    }

    @Test
    void cookRecipe_returnsOk() throws Exception {
        doNothing().when(recipeService).cookRecipe(1L);

        mockMvc.perform(post("/api/recipes/1/cook"))
                .andExpect(status().isOk());
    }

    @Test
    void addRecipe_acceptsJsonAndReturnsCreatedRecipe() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setName("Soup");
        recipe.setInstructions("Simmer slowly");

        when(recipeService.addRecipe(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe created = invocation.getArgument(0);
            created.setId(2L);
            return created;
        });

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Soup"));
    }
}
