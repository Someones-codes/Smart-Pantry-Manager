package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Shows ONLY the recipes the user can make with the current pantry
 * (the strict-matching rule lives in RecipeMatcher).
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView rvRecipes;
    private TextView textEmptyMessage;
    private ArrayList<Recipe> suggestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        rvRecipes = findViewById(R.id.rvRecipes);
        textEmptyMessage = findViewById(R.id.textEmptyMessage);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        NavBar.setup(this, R.id.navRecipes);
    }

    /** Re-run the matching every time the screen appears, so it always reflects the current pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            ArrayList<PantryItem> pantry = ds.getPantryItems();
            ArrayList<Recipe> allRecipes = ds.getAllRecipes();
            ds.close();

            // The strict-matching rule decides which recipes qualify
            suggestions = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);

            RecipeAdapter adapter = new RecipeAdapter(suggestions, this);
            adapter.setOnItemClickListener(v -> {
                RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) v.getTag();
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, suggestions.get(position).getId());
                    startActivity(intent);
                }
            });
            rvRecipes.setAdapter(adapter);

            // Zero matches: show a helpful message instead of a blank screen
            boolean isEmpty = suggestions.isEmpty();
            textEmptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvRecipes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, R.string.load_error, Toast.LENGTH_LONG).show();
        }
    }
}