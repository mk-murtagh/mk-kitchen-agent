package com.marykatekitchen.mykitchen_agent.repository;

import com.marykatekitchen.mykitchen_agent.model.Ingredient;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
    List<Ingredient> findByExpirationDateBetweenOrderByExpirationDateAsc(
            LocalDate startDate,
            LocalDate endDate
    );
}
