package com.marykatekitchen.mykitchen_agent.service;

import com.marykatekitchen.mykitchen_agent.model.Ingredient;
import com.marykatekitchen.mykitchen_agent.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void getAllIngredients_returnsRepositoryResults() {
        Ingredient milk =
                new Ingredient("Milk", 2.0, "cup", "fridge", null);

        when(ingredientRepository.findAll())
                .thenReturn(List.of(milk));

        List<Ingredient> ingredients =
                ingredientService.getAllIngredients();

        assertEquals(1, ingredients.size());
        assertEquals("Milk", ingredients.get(0).getName());

        verify(ingredientRepository).findAll();
    }

    @Test
    void addIngredient_savesIngredient() {
        Ingredient milk =
                new Ingredient("Milk", 2.0, "cup", "fridge", null);

        when(ingredientRepository.save(milk))
                .thenReturn(milk);

        Ingredient saved =
                ingredientService.addIngredient(milk);

        assertSame(milk, saved);
        verify(ingredientRepository).save(milk);
    }

    @Test
    void updateIngredient_updatesFieldsAndSaves() {
        Ingredient existing =
                new Ingredient(
                        "Milk",
                        1.0,
                        "cup",
                        "fridge",
                        LocalDate.of(2026, 8, 10)
                );

        Ingredient updated =
                new Ingredient(
                        "Whole Milk",
                        2.0,
                        "cup",
                        "fridge",
                        LocalDate.of(2026, 8, 12)
                );

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(ingredientRepository.save(existing))
                .thenReturn(existing);

        Ingredient result =
                ingredientService.updateIngredient(1L, updated);

        assertEquals("Whole Milk", result.getName());
        assertEquals(2.0, result.getQuantity());
        assertEquals("cup", result.getUnit());
        assertEquals("fridge", result.getLocation());
        assertEquals(
                LocalDate.of(2026, 8, 12),
                result.getExpirationDate()
        );

        verify(ingredientRepository).save(existing);
    }

    @Test
    void deleteIngredient_deletesById() {
        ingredientService.deleteIngredient(4L);

        verify(ingredientRepository).deleteById(4L);
    }

    @Test
    void getExpiringIngredients_returnsIngredientsWithinRequestedWindow() {
        Ingredient milk =
                new Ingredient(
                        "Milk",
                        1.0,
                        "cup",
                        "fridge",
                        LocalDate.now().plusDays(2)
                );

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        when(
                ingredientRepository
                        .findByExpirationDateBetweenOrderByExpirationDateAsc(
                                today,
                                endDate
                        )
        ).thenReturn(List.of(milk));

        List<Ingredient> result =
                ingredientService.getExpiringIngredients(7);

        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());

        verify(ingredientRepository)
                .findByExpirationDateBetweenOrderByExpirationDateAsc(
                        today,
                        endDate
                );
    }
}