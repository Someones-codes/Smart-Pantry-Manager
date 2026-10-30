package com.smartpantry.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/** Pantry screen: shows every ingredient the user owns and lets them delete items. */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvPantry;
    private TextView textEmptyPantry;
    private SwitchCompat switchDelete;
    private PantryAdapter pantryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ... KEEP the template's existing lines here (EdgeToEdge, setContentView, insets) ...
        setContentView(R.layout.activity_main);

        rvPantry = findViewById(R.id.rvPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);
        switchDelete = findViewById(R.id.switchDelete);

        // LayoutManager decides how rows are arranged: a normal vertical list
        rvPantry.setLayoutManager(new LinearLayoutManager(this));

        initDeleteSwitch();
        addSampleDataIfEmpty();   // TEMPORARY: remove once the Add screen exists
    }

    /** Runs every time the screen becomes visible, so the list is always fresh. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /** Reads all pantry items from the database and shows them in the RecyclerView. */
    private void loadPantry() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            ArrayList<PantryItem> items = ds.getPantryItems();
            ds.close();

            pantryAdapter = new PantryAdapter(items, this);
            pantryAdapter.setDeleting(switchDelete.isChecked());
            pantryAdapter.setOnDataChanged(this::updateEmptyState);
            rvPantry.setAdapter(pantryAdapter);
            updateEmptyState();
        } catch (Exception e) {
            Toast.makeText(this, R.string.load_error, Toast.LENGTH_LONG).show();
        }
    }

    /** Shows the "pantry is empty" message instead of a blank screen. */
    private void updateEmptyState() {
        boolean isEmpty = pantryAdapter.getItemCount() == 0;
        textEmptyPantry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /** The Delete switch turns delete mode on/off in the adapter. */
    private void initDeleteSwitch() {
        switchDelete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pantryAdapter != null) {
                pantryAdapter.setDeleting(isChecked);
            }
        });
    }

    /** TEMPORARY test helper: inserts 3 sample items if the pantry is empty. */
    private void addSampleDataIfEmpty() {
        PantryDataSource ds = new PantryDataSource(this);
        ds.open();
        if (ds.getPantryItems().isEmpty()) {
            ds.insertPantryItem(new PantryItem(-1, "tomatoes", 3, "pcs", ""));
            ds.insertPantryItem(new PantryItem(-1, "flour", 500, "g", "2026-12-01"));
            ds.insertPantryItem(new PantryItem(-1, "milk", 0.5, "l", ""));
        }
        ds.close();
    }
}