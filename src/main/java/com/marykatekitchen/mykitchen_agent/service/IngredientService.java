package com.marykatekitchen.mykitchen_agent.service;

import com.marykatekitchen.mykitchen_agent.model.Ingredient;
import com.marykatekitchen.mykitchen_agent.repository.IngredientRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public Ingredient addIngredient(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    public Ingredient updateIngredient(Long id, Ingredient updatedIngredient) {
        return ingredientRepository.findById(id)
                .map(ingredient -> {
                    ingredient.setName(updatedIngredient.getName());
                    ingredient.setQuantity(updatedIngredient.getQuantity());
                    ingredient.setUnit(updatedIngredient.getUnit());
                    ingredient.setLocation(updatedIngredient.getLocation());
                    ingredient.setExpirationDate(updatedIngredient.getExpirationDate());
                    return ingredientRepository.save(ingredient);
                })
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id " + id));
    }

    public void deleteIngredient(Long id) {
        ingredientRepository.deleteById(id);
    }

    public List<Ingredient> getExpiringIngredients(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        return ingredientRepository
            .findByExpirationDateBetweenOrderByExpirationDateAsc(
                    today,
                    endDate
            );
    }
}
