package com.smartpantry.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/** Displays the suggested recipes in the RecyclerView (name + number of ingredients). */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final ArrayList<Recipe> recipeData;
    private final Context parentContext;
    private View.OnClickListener onItemClickListener;

    public RecipeAdapter(ArrayList<Recipe> data, Context context) {
        this.recipeData = data;
        this.parentContext = context;
    }

    /** Holds the widgets of one recipe row. */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public final TextView textName;
        public final TextView textSummary;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textSummary = itemView.findViewById(R.id.textRecipeSummary);
        }
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeData.get(position);
        holder.textName.setText(recipe.getName());
        holder.textSummary.setText(parentContext.getString(
                R.string.ingredient_count, recipe.getIngredients().size()));
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return recipeData.size();
    }
}