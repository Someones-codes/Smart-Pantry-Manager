package com.smartpantry.manager;

/**
 * One ingredient line that a recipe requires,
 * e.g. "2 tomatoes" or "250 g flour".
 * Maps to one row in the recipe_ingredient table.
 */
public class RecipeIngredient {

    private int id;
    private int recipeId;        // foreign key pointing at recipe._id
    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient() {
        this.id = -1;
    }

    public RecipeIngredient(int id, int recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}