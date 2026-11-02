package com.smartpantry.manager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Shows one recipe in full: its ingredients with quantities, and the preparation steps. */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key used to pass the recipe id from SuggestedRecipesActivity through the Intent. */
    public static final String EXTRA_RECIPE_ID = "recipeId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);

        PantryDataSource ds = new PantryDataSource(this);
        Recipe recipe = null;
        try {
            ds.open();
            recipe = ds.getRecipe(recipeId);
            ds.close();
        } catch (Exception e) {
            recipe = null;
        }

        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.textRecipeTitle)).setText(recipe.getName());
        ((TextView) findViewById(R.id.textRecipeSteps)).setText(recipe.getSteps());

        // Build the ingredient list, one bullet per line
        StringBuilder list = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            list.append("\u2022 ")
                    .append(formatQuantity(ing.getQuantity())).append(" ")
                    .append(ing.getUnit()).append(" ")
                    .append(ing.getName()).append("\n");
        }
        ((TextView) findViewById(R.id.textRecipeIngredients)).setText(list.toString().trim());

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}