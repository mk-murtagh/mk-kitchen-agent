package com.marykatekitchen.mykitchen_agent.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Integer prepTimeMinutes;

    private Integer cookTimeMinutes;

    private Integer servings;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Column(length = 5000)
    private String instructions;

    public Recipe() {
    }

    public Recipe(
            String name,
            String description,
            Integer prepTimeMinutes,
            Integer cookTimeMinutes,
            Integer servings,
            String instructions,
            List<RecipeIngredient> ingredients) {

        this.name = name;
        this.description = description;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.servings = servings;
        this.instructions = instructions;
        this.ingredients = ingredients;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public Integer getServings() {
        return servings;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrepTimeMinutes(Integer prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;

        if (ingredients != null) {
            for (RecipeIngredient ingredient : ingredients) {
                ingredient.setRecipe(this);
            }
        }
    }
}