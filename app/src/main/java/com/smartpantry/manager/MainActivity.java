
package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Pantry screen:
 * Displays all ingredients owned by the user.
 * Allows editing and deleting pantry items.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvPantry;
    private TextView textEmptyPantry;
    private SwitchCompat switchDelete;

    private PantryAdapter pantryAdapter;
    private ArrayList<PantryItem> pantryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Connect Java variables to XML views
        rvPantry = findViewById(R.id.rvPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);
        switchDelete = findViewById(R.id.switchDelete);

        // Display ingredients vertically
        rvPantry.setLayoutManager(new LinearLayoutManager(this));

        // Initialize screen functionality
        initDeleteSwitch();
        initAddButton();

        // Temporary test data
        addSampleDataIfEmpty();
    }

    /**
     * Reload the pantry whenever the user returns to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /**
     * Load pantry ingredients from SQLite.
     */
    private void loadPantry() {

        PantryDataSource ds = new PantryDataSource(this);

        try {
            ds.open();

            pantryItems = ds.getPantryItems();

            pantryAdapter = new PantryAdapter(pantryItems, this);

            // Preserve the current delete mode
            pantryAdapter.setDeleting(switchDelete.isChecked());

            // Update the empty-state message after data changes
            pantryAdapter.setOnDataChanged(this::updateEmptyState);

            // Handle clicks on individual ingredients
            pantryAdapter.setOnItemClickListener(v -> {

                RecyclerView.ViewHolder holder =
                        (RecyclerView.ViewHolder) v.getTag();

                int position = holder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {

                    int itemId = pantryItems.get(position).getId();

                    Intent intent = new Intent(
                            MainActivity.this,
                            AddEditIngredientActivity.class
                    );

                    intent.putExtra(
                            AddEditIngredientActivity.EXTRA_ITEM_ID,
                            itemId
                    );

                    startActivity(intent);
                }
            });

            rvPantry.setAdapter(pantryAdapter);

            updateEmptyState();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    R.string.load_error,
                    Toast.LENGTH_LONG
            ).show();

            e.printStackTrace();

        } finally {
            ds.close();
        }
    }

    /**
     * Display an empty message when no ingredients exist.
     */
    private void updateEmptyState() {

        boolean isEmpty =
                pantryAdapter == null || pantryAdapter.getItemCount() == 0;

        textEmptyPantry.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        rvPantry.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE
        );
    }

    /**
     * Enable or disable deletion mode.
     */
    private void initDeleteSwitch() {

        switchDelete.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (pantryAdapter != null) {
                        pantryAdapter.setDeleting(isChecked);
                    }
                }
        );
    }

    /**
     * Open the ingredient form in add mode.
     */
    private void initAddButton() {

        findViewById(R.id.buttonAddItem).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AddEditIngredientActivity.class
            );

            startActivity(intent);
        });
    }

    /**
     * TEMPORARY:
     * Insert sample ingredients for testing.
     * Remove this method and its call once real ingredient entry works.
     */
    private void addSampleDataIfEmpty() {

        PantryDataSource ds = new PantryDataSource(this);

        try {
            ds.open();

            if (ds.getPantryItems().isEmpty()) {

                ds.insertPantryItem(
                        new PantryItem(-1, "tomatoes", 3, "pcs", "")
                );

                ds.insertPantryItem(
                        new PantryItem(-1, "flour", 500, "g", "2026-12-01")
                );

                ds.insertPantryItem(
                        new PantryItem(-1, "milk", 0.5, "l", "")
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            ds.close();
        }
    }
}
