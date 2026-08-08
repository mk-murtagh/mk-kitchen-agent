package com.marykatekitchen.mykitchen_agent.repository;

import com.marykatekitchen.mykitchen_agent.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}