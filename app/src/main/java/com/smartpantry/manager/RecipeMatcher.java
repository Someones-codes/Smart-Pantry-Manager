package com.smartpantry.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic: decides which recipes the user can make RIGHT NOW.
 *
 * STRICT RULE: a recipe is suggested only if EVERY ingredient it requires is in the
 * pantry in at least the required quantity. A single missing or insufficient
 * ingredient excludes the whole recipe. There are no partial matches.
 *
 * This class contains no Android code, so it can be unit tested on its own.
 */
public class RecipeMatcher {

    /** Tiny tolerance so decimal rounding (e.g. 0.1 + 0.2) cannot wrongly fail a match. */
    private static final double EPSILON = 1e-9;

    private RecipeMatcher() { }   // utility class: never instantiated

    // =====================================================================
    //  1. NAME NORMALISATION
    // =====================================================================

    /**
     * Makes ingredient names comparable: trims, lowercases, collapses spaces and strips
     * common plural endings. Used on BOTH pantry and recipe names, so even if a rule is
     * imperfect for some rare word, both sides still end up identical.
     * Examples: "Tomatoes " -> "tomato", "eggs" -> "egg", "berries" -> "berry",
     *           "peaches" -> "peach", "olives" -> "olive", "glass" -> "glass".
     */
    public static String normalise(String name) {
        if (name == null) {
            return "";
        }
        String n = name.trim().toLowerCase(Locale.US).replaceAll("\\s+", " ");

        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";        // berries -> berry
        }
        if (n.endsWith("oes") || n.endsWith("ches") || n.endsWith("shes")
                || n.endsWith("xes") || n.endsWith("sses")) {
            return n.substring(0, n.length() - 2);              // tomatoes -> tomato, peaches -> peach
        }
        if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 3) {
            return n.substring(0, n.length() - 1);              // eggs -> egg, olives -> olive
        }
        return n;
    }

    // =====================================================================
    //  2. UNIT FAMILIES AND CONVERSION
    // =====================================================================

    /** Which family a unit belongs to. Only units in the SAME family can be compared. */
    private static String unitFamily(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.US);
        switch (u) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
                return "volume";
            case "pcs":
                return "count";
            default:
                return "other:" + u;   // an unknown unit only matches itself
        }
    }

    /** Converts a quantity to its family's base unit: grams, millilitres, or pieces. */
    private static double toBaseUnit(double quantity, String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.US);
        switch (u) {
            case "kg":
            case "l":
                return quantity * 1000;   // 1 kg = 1000 g, 1 l = 1000 ml
            case "tbsp":
                return quantity * 15;     // 1 tablespoon = 15 ml
            case "tsp":
                return quantity * 5;      // 1 teaspoon = 5 ml
            default:
                return quantity;          // g, ml, pcs, or unknown: already the base unit
        }
    }

    /** Lookup key = normalised name + unit family, e.g. "tomato|count" or "flour|mass". */
    private static String makeKey(String name, String unit) {
        return normalise(name) + "|" + unitFamily(unit);
    }

    // =====================================================================
    //  3. THE STRICT MATCH
    // =====================================================================

    /**
     * Adds up the pantry per (ingredient, unit family) in base units.
     * Duplicate rows for the same ingredient are summed together.
     */
    public static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = makeKey(item.getName(), item.getUnit());
            double baseQuantity = toBaseUnit(item.getQuantity(), item.getUnit());
            Double existing = totals.get(key);
            totals.put(key, (existing == null ? 0.0 : existing) + baseQuantity);
        }
        return totals;
    }

    /**
     * THE STRICT RULE. Returns true only if every required ingredient is available
     * in sufficient quantity. Returns false the moment ANY ingredient is missing
     * or short, and for a recipe that lists no ingredients at all.
     */
    public static boolean canMake(Recipe recipe, Map<String, Double> pantryTotals) {
        List<RecipeIngredient> required = recipe.getIngredients();
        if (required == null || required.isEmpty()) {
            return false;
        }
        for (RecipeIngredient need : required) {
            Double have = pantryTotals.get(makeKey(need.getName(), need.getUnit()));
            if (have == null) {
                return false;                                   // ingredient not in pantry at all
            }
            double needed = toBaseUnit(need.getQuantity(), need.getUnit());
            if (have + EPSILON < needed) {
                return false;                                   // in pantry, but not enough
            }
        }
        return true;                                            // every single ingredient passed
    }

    /** Returns only the recipes the user can make with the current pantry. */
    public static ArrayList<Recipe> getSuggestedRecipes(List<Recipe> allRecipes,
                                                        List<PantryItem> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        ArrayList<Recipe> suggestions = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, totals)) {
                suggestions.add(recipe);
            }
        }
        return suggestions;
    }
}