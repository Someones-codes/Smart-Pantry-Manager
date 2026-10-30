package com.smartpantry.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates and upgrades the SQLite database for Smart Pantry Manager.
 * Tables: pantry_item, recipe, recipe_ingredient.
 * On first launch it also inserts 20 predefined recipes (seeding).
 */
public class PantryDBHelper extends SQLiteOpenHelper {

    // ---- Database file name and version ----
    private static final String DATABASE_NAME = "smartpantry.db";
    // Increase this number whenever the table structure changes (triggers onUpgrade)
    private static final int DATABASE_VERSION = 1;

    // ---- Table and column names (public so other classes can reuse them) ----
    public static final String TABLE_PANTRY = "pantry_item";
    public static final String TABLE_RECIPE = "recipe";
    public static final String TABLE_RECIPE_ING = "recipe_ingredient";

    public static final String COL_ID = "_id";
    public static final String COL_NAME = "ingredient_name";   // used by pantry_item AND recipe_ingredient
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    public static final String COL_RECIPE_NAME = "recipe_name";
    public static final String COL_STEPS = "steps";
    public static final String COL_RECIPE_ID = "recipe_id";    // foreign key in recipe_ingredient

    // ---- SQL statements that create each table ----
    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_EXPIRY + " TEXT);";

    private static final String CREATE_TABLE_RECIPE =
            "CREATE TABLE " + TABLE_RECIPE + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_NAME + " TEXT NOT NULL, "
                    + COL_STEPS + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_RECIPE_ING =
            "CREATE TABLE " + TABLE_RECIPE_ING + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_ID + " INTEGER NOT NULL, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES "
                    + TABLE_RECIPE + "(" + COL_ID + ") ON DELETE CASCADE);";

    /** Constructor: just passes the name and version to the parent class. */
    public PantryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Runs before onCreate/onUpgrade; switches on foreign key enforcement. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Called the first time the database is created: build tables, then seed recipes. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE_ING);
        seedRecipes(db);
    }

    /**
     * Called when DATABASE_VERSION increases. Here we simply drop and rebuild
     * everything. (Fine during development; a released app would use ALTER TABLE
     * so user data is not lost, as the study guide explains.)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // =====================================================================
    //  SEED DATA: 20 predefined recipes
    //  Ingredient rows are {name, quantity, unit}. Units used: g, ml, pcs, tsp, tbsp.
    //  Many recipes share ingredients (eggs, onions, garlic...) so adding or
    //  removing one pantry item visibly changes the suggestions.
    // =====================================================================
    private void seedRecipes(SQLiteDatabase db) {

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until tender and drain.\n2. Fry the garlic in olive oil for 1 minute.\n3. Add chopped tomatoes and salt, simmer for 10 minutes.\n4. Toss the pasta through the sauce and serve.",
                new String[][]{{"pasta", "200", "g"}, {"tomatoes", "3", "pcs"}, {"garlic", "2", "pcs"},
                        {"olive oil", "2", "tbsp"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Garlic Bread",
                "1. Mash the crushed garlic into the soft butter.\n2. Spread on the bread slices.\n3. Bake at 200 C for 8 to 10 minutes until golden.",
                new String[][]{{"bread", "4", "pcs"}, {"garlic", "2", "pcs"}, {"butter", "50", "g"}});

        addRecipe(db, "Egg Fried Rice",
                "1. Fry the chopped onion in oil until soft.\n2. Push aside, scramble the eggs in the pan.\n3. Add cooked rice and soy sauce.\n4. Stir-fry for 5 minutes and serve hot.",
                new String[][]{{"rice", "300", "g"}, {"eggs", "2", "pcs"}, {"soy sauce", "2", "tbsp"},
                        {"onions", "1", "pcs"}, {"vegetable oil", "1", "tbsp"}});

        addRecipe(db, "Pancakes",
                "1. Whisk flour, sugar, eggs and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in batter and cook until bubbles form, then flip.\n4. Cook until golden on both sides.",
                new String[][]{{"flour", "200", "g"}, {"eggs", "2", "pcs"}, {"milk", "300", "ml"},
                        {"sugar", "2", "tbsp"}, {"butter", "20", "g"}});

        addRecipe(db, "Greek Salad",
                "1. Chop the cucumber, tomatoes and onion.\n2. Crumble the feta over the top.\n3. Drizzle with olive oil and serve fresh.",
                new String[][]{{"cucumbers", "1", "pcs"}, {"tomatoes", "2", "pcs"}, {"onions", "1", "pcs"},
                        {"feta cheese", "100", "g"}, {"olive oil", "2", "tbsp"}});

        addRecipe(db, "Plain Omelette",
                "1. Beat the eggs with salt and pepper.\n2. Melt butter in a pan over medium heat.\n3. Pour in eggs, cook until set, fold and serve.",
                new String[][]{{"eggs", "3", "pcs"}, {"butter", "10", "g"}, {"salt", "1", "tsp"},
                        {"black pepper", "0.5", "tsp"}});

        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Melt butter in a pan and add beaten eggs with salt.\n3. Stir gently until just set.\n4. Pile onto the toast.",
                new String[][]{{"eggs", "2", "pcs"}, {"bread", "2", "pcs"}, {"butter", "10", "g"},
                        {"salt", "0.5", "tsp"}});

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread slices.\n2. Place the cheese between the slices.\n3. Fry on both sides until golden and the cheese melts.",
                new String[][]{{"bread", "2", "pcs"}, {"cheese", "50", "g"}, {"butter", "10", "g"}});

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter and warm milk.\n3. Season with salt.",
                new String[][]{{"potatoes", "4", "pcs"}, {"butter", "30", "g"}, {"milk", "100", "ml"},
                        {"salt", "1", "tsp"}});

        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic in oil until soft.\n2. Add chopped tomatoes and salt.\n3. Simmer for 20 minutes, then blend until smooth.",
                new String[][]{{"tomatoes", "6", "pcs"}, {"onions", "1", "pcs"}, {"garlic", "2", "pcs"},
                        {"vegetable oil", "1", "tbsp"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Vegetable Stir-Fry",
                "1. Slice the carrots and onion thinly.\n2. Heat oil and fry the garlic for 30 seconds.\n3. Add vegetables and stir-fry for 5 minutes.\n4. Splash in soy sauce and serve.",
                new String[][]{{"carrots", "2", "pcs"}, {"onions", "1", "pcs"}, {"soy sauce", "2", "tbsp"},
                        {"vegetable oil", "1", "tbsp"}, {"garlic", "2", "pcs"}});

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas in a bowl.\n2. Mix in eggs and flour to make a batter.\n3. Fry spoonfuls in a pan until golden on both sides.",
                new String[][]{{"bananas", "2", "pcs"}, {"eggs", "2", "pcs"}, {"flour", "100", "g"}});

        addRecipe(db, "Banana Oatmeal",
                "1. Heat the milk in a pot.\n2. Stir in the oats and cook for 5 minutes.\n3. Top with sliced banana and sugar.",
                new String[][]{{"oats", "80", "g"}, {"milk", "250", "ml"}, {"bananas", "1", "pcs"},
                        {"sugar", "1", "tbsp"}});

        addRecipe(db, "Egg Fried Noodles",
                "1. Cook the noodles and drain.\n2. Fry onion and garlic in a pan.\n3. Scramble in the eggs.\n4. Add noodles and soy sauce, toss and serve.",
                new String[][]{{"noodles", "200", "g"}, {"eggs", "2", "pcs"}, {"soy sauce", "2", "tbsp"},
                        {"onions", "1", "pcs"}, {"garlic", "1", "pcs"}});

        addRecipe(db, "Cheesy Omelette",
                "1. Beat the eggs with salt.\n2. Cook in melted butter until almost set.\n3. Add grated cheese, fold and serve.",
                new String[][]{{"eggs", "3", "pcs"}, {"cheese", "40", "g"}, {"butter", "10", "g"},
                        {"salt", "0.5", "tsp"}});

        addRecipe(db, "Rice and Beans",
                "1. Fry onion and garlic in oil.\n2. Add the beans and a pinch of salt.\n3. Serve over cooked rice.",
                new String[][]{{"rice", "200", "g"}, {"beans", "200", "g"}, {"onions", "1", "pcs"},
                        {"garlic", "2", "pcs"}, {"vegetable oil", "1", "tbsp"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Tuna Pasta",
                "1. Boil and drain the pasta.\n2. Fry the onion in olive oil.\n3. Add the tuna and salt, heat through.\n4. Mix with the pasta.",
                new String[][]{{"pasta", "200", "g"}, {"tuna", "1", "pcs"}, {"onions", "1", "pcs"},
                        {"olive oil", "1", "tbsp"}, {"salt", "0.5", "tsp"}});

        addRecipe(db, "Chicken and Rice",
                "1. Season the chicken with salt and brown in oil.\n2. Add chopped onion and garlic.\n3. Add the rice and enough water to cook it through.\n4. Cover and simmer until the rice is tender.",
                new String[][]{{"chicken", "400", "g"}, {"rice", "200", "g"}, {"onions", "1", "pcs"},
                        {"garlic", "2", "pcs"}, {"salt", "1", "tsp"}, {"vegetable oil", "1", "tbsp"}});

        addRecipe(db, "Avocado Toast",
                "1. Toast the bread.\n2. Mash the avocado with lemon juice and salt.\n3. Spread on the toast.",
                new String[][]{{"bread", "2", "pcs"}, {"avocados", "1", "pcs"}, {"lemons", "1", "pcs"},
                        {"salt", "0.5", "tsp"}});

        addRecipe(db, "Roast Potatoes",
                "1. Cut the potatoes into chunks.\n2. Toss with olive oil, crushed garlic and salt.\n3. Roast at 220 C for 40 minutes, turning once.",
                new String[][]{{"potatoes", "6", "pcs"}, {"olive oil", "2", "tbsp"}, {"garlic", "2", "pcs"},
                        {"salt", "1", "tsp"}});
    }

    /**
     * Inserts one recipe and all of its ingredient rows.
     * @param ingredients each row is {name, quantity, unit}
     */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String[][] ingredients) {
        // 1. Insert the recipe itself; insert() returns the new row's _id
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPE, null, recipeValues);

        // 2. Insert each required ingredient, linked to the recipe by recipe_id
        for (String[] ing : ingredients) {
            ContentValues ingValues = new ContentValues();
            ingValues.put(COL_RECIPE_ID, recipeId);
            ingValues.put(COL_NAME, ing[0]);
            ingValues.put(COL_QUANTITY, Double.parseDouble(ing[1]));
            ingValues.put(COL_UNIT, ing[2]);
            db.insert(TABLE_RECIPE_ING, null, ingValues);
        }
    }
}