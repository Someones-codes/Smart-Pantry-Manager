package com.smartpantry.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Data access class: opens and closes the database and provides every
 * query the app needs (CRUD for pantry items, read access for recipes).
 * Usage pattern: open() -> do the operation -> close().
 */
public class PantryDataSource {

    private SQLiteDatabase database;        // the open database connection
    private final PantryDBHelper dbHelper;  // creates/upgrades the database

    /** Creates the helper; the database itself is not opened until open() is called. */
    public PantryDataSource(Context context) {
        dbHelper = new PantryDBHelper(context);
    }

    /** Opens the database for reading and writing. */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /** Closes the database. Always call this when finished. */
    public void close() {
        dbHelper.close();
    }

    // =====================================================================
    //  PANTRY ITEMS: CREATE, READ, UPDATE, DELETE
    // =====================================================================

    /** CREATE: inserts a new pantry item. Returns true if it was saved. */
    public boolean insertPantryItem(PantryItem item) {
        boolean didSucceed = false;
        try {
            ContentValues values = new ContentValues();
            values.put(PantryDBHelper.COL_NAME, item.getName());
            values.put(PantryDBHelper.COL_QUANTITY, item.getQuantity());
            values.put(PantryDBHelper.COL_UNIT, item.getUnit());
            values.put(PantryDBHelper.COL_EXPIRY, item.getExpiryDate());
            // insert() returns the new row id, or -1 if it failed
            didSucceed = database.insert(PantryDBHelper.TABLE_PANTRY, null, values) > 0;
        } catch (Exception e) {
            // leave didSucceed as false so the caller can warn the user
        }
        return didSucceed;
    }

    /** READ: returns every pantry item, sorted alphabetically by name. */
    public ArrayList<PantryItem> getPantryItems() {
        ArrayList<PantryItem> items = new ArrayList<>();
        try {
            Cursor cursor = database.query(PantryDBHelper.TABLE_PANTRY, null, null, null,
                    null, null, PantryDBHelper.COL_NAME + " COLLATE NOCASE ASC");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                items.add(cursorToPantryItem(cursor));
                cursor.moveToNext();   // forgetting this causes an infinite loop
            }
            cursor.close();
        } catch (Exception e) {
            items = new ArrayList<>(); // return an empty list rather than crash
        }
        return items;
    }

    /** READ: returns one pantry item by id, or null if not found. */
    public PantryItem getPantryItem(int id) {
        PantryItem item = null;
        try {
            Cursor cursor = database.query(PantryDBHelper.TABLE_PANTRY, null,
                    PantryDBHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                item = cursorToPantryItem(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            item = null;
        }
        return item;
    }

    /** UPDATE: overwrites the row whose _id matches item.getId(). Returns true if updated. */
    public boolean updatePantryItem(PantryItem item) {
        boolean didSucceed = false;
        try {
            ContentValues values = new ContentValues();
            values.put(PantryDBHelper.COL_NAME, item.getName());
            values.put(PantryDBHelper.COL_QUANTITY, item.getQuantity());
            values.put(PantryDBHelper.COL_UNIT, item.getUnit());
            values.put(PantryDBHelper.COL_EXPIRY, item.getExpiryDate());
            // update() returns the number of rows changed
            didSucceed = database.update(PantryDBHelper.TABLE_PANTRY, values,
                    PantryDBHelper.COL_ID + " = ?",
                    new String[]{String.valueOf(item.getId())}) > 0;
        } catch (Exception e) {
            // didSucceed stays false
        }
        return didSucceed;
    }

    /** DELETE: removes the pantry item with the given id. Returns true if a row was deleted. */
    public boolean deletePantryItem(int id) {
        boolean didDelete = false;
        try {
            didDelete = database.delete(PantryDBHelper.TABLE_PANTRY,
                    PantryDBHelper.COL_ID + " = ?",
                    new String[]{String.valueOf(id)}) > 0;
        } catch (Exception e) {
            // didDelete stays false
        }
        return didDelete;
    }

    // =====================================================================
    //  RECIPES: READ ONLY (they are seeded, the user does not edit them)
    // =====================================================================

    /** READ: returns every recipe, each with its full ingredient list filled in. */
    public ArrayList<Recipe> getAllRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            Cursor cursor = database.query(PantryDBHelper.TABLE_RECIPE, null, null, null,
                    null, null, PantryDBHelper.COL_RECIPE_NAME + " ASC");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Recipe recipe = cursorToRecipe(cursor);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                recipes.add(recipe);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            recipes = new ArrayList<>();
        }
        return recipes;
    }

    /** READ: returns one recipe (with ingredients) by id, or null if not found. */
    public Recipe getRecipe(int id) {
        Recipe recipe = null;
        try {
            Cursor cursor = database.query(PantryDBHelper.TABLE_RECIPE, null,
                    PantryDBHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                recipe = cursorToRecipe(cursor);
                recipe.setIngredients(getIngredientsForRecipe(id));
            }
            cursor.close();
        } catch (Exception e) {
            recipe = null;
        }
        return recipe;
    }

    /** Reads all ingredient rows that belong to one recipe (matched by recipe_id). */
    private ArrayList<RecipeIngredient> getIngredientsForRecipe(int recipeId) {
        ArrayList<RecipeIngredient> list = new ArrayList<>();
        Cursor cursor = database.query(PantryDBHelper.TABLE_RECIPE_ING, null,
                PantryDBHelper.COL_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RecipeIngredient ing = new RecipeIngredient();
            ing.setId(cursor.getInt(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_ID)));
            ing.setRecipeId(recipeId);
            ing.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_NAME)));
            ing.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_QUANTITY)));
            ing.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_UNIT)));
            list.add(ing);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    // =====================================================================
    //  HELPERS: convert the cursor's current row into a Java object
    // =====================================================================

    private PantryItem cursorToPantryItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_NAME)));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_QUANTITY)));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_UNIT)));
        String expiry = cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_EXPIRY));
        item.setExpiryDate(expiry == null ? "" : expiry);   // the column can be NULL
        return item;
    }

    private Recipe cursorToRecipe(Cursor cursor) {
        Recipe recipe = new Recipe();
        recipe.setId(cursor.getInt(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_ID)));
        recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_RECIPE_NAME)));
        recipe.setSteps(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_STEPS)));
        return recipe;
    }
}