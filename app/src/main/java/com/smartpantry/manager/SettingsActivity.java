package com.smartpantry.manager;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Settings screen. Stores the user's expiry-highlight preference in SharedPreferences,
 * which keeps small key/value settings that persist after the app is closed.
 */
public class SettingsActivity extends AppCompatActivity {

    /** Name of the preferences file and the key used by the adapter to read the setting. */
    public static final String PREFS_NAME = "SmartPantryPrefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        NavBar.setup(this, R.id.navSettings);

        SwitchCompat switchAlerts = findViewById(R.id.switchExpiryAlerts);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Show the saved value first (default: on), THEN attach the listener
        switchAlerts.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));

        // Save immediately whenever the user flips the switch; apply() writes in the background
        switchAlerts.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());
    }
}