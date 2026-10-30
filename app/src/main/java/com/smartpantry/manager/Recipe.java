package com.smartpantry.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe with its preparation steps and the list of ingredients it requires.
 * The ingredient list is filled in by the data source after the recipe is read.
 */
public class Recipe {

    private int id;
    private String name;
    private String steps;                         // preparation instructions
    private List<RecipeIngredient> ingredients;   // everything this recipe needs

    public Recipe() {
        this.id = -1;
        this.ingredients = new ArrayList<>();
    }

    public Recipe(int id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}