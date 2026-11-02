package com.smartpantry.manager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.Button;

/**
 * Wires up the bottom navigation bar (bottom_nav.xml) on any screen.
 * Each button starts its Activity with an Intent. FLAG_ACTIVITY_CLEAR_TOP stops
 * duplicate copies of a screen piling up in the back stack.
 */
public class NavBar {

    private NavBar() { }

    /** @param currentId the id of the nav button for the screen we are on (it is highlighted) */
    public static void setup(Activity activity, int currentId) {
        Button pantry = activity.findViewById(R.id.navPantry);
        Button recipes = activity.findViewById(R.id.navRecipes);
        Button settings = activity.findViewById(R.id.navSettings);

        styleButton(pantry, currentId == R.id.navPantry);
        styleButton(recipes, currentId == R.id.navRecipes);
        styleButton(settings, currentId == R.id.navSettings);

        pantry.setOnClickListener(v -> go(activity, MainActivity.class));
        recipes.setOnClickListener(v -> go(activity, SuggestedRecipesActivity.class));
        // The Settings button is wired in the next step, when SettingsActivity exists.
        settings.setOnClickListener(v -> go(activity, SettingsActivity.class));
    }

    /** Opens another screen from the nav bar. */
    private static void go(Activity from, Class<?> target) {
        Intent intent = new Intent(from, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        from.startActivity(intent);
    }

    /** The current screen's button is bold, full-strength and disabled; the others are dimmed. */
    private static void styleButton(Button button, boolean isCurrent) {
        button.setEnabled(!isCurrent);
        button.setAlpha(isCurrent ? 1f : 0.65f);
        button.setTypeface(null, isCurrent ? Typeface.BOLD : Typeface.NORMAL);
    }
}