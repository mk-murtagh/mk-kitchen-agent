package com.marykatekitchen.mykitchen_agent.repository;

import com.marykatekitchen.mykitchen_agent.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
}
