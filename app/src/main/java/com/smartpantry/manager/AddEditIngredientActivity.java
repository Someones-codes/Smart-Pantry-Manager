package com.smartpantry.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

/**
 * Form screen used for BOTH adding a new pantry item and editing an existing one.
 * If an ingredient id arrives in the Intent extras, the form loads that item (edit mode);
 * otherwise it starts blank (add mode).
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Key used to pass the pantry item id from MainActivity through the Intent. */
    public static final String EXTRA_ITEM_ID = "pantryItemId";

    private EditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private TextView textFormTitle;
    private int itemId = -1;   // -1 = new item (insert); otherwise the id being edited (update)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        // Connect Java variables to the widgets in the XML layout
        textFormTitle = findViewById(R.id.textFormTitle);
        editName = findViewById(R.id.editIngredientName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);

        // Fill the unit dropdown from the string-array in strings.xml
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        initExpiryPicker();
        initButtons();

        // Did MainActivity send us an id? If so, we are editing.
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            textFormTitle.setText(R.string.title_edit_ingredient);
            loadItem(itemId);
        }
    }

    /** Loads an existing item from the database into the form fields (edit mode). */
    private void loadItem(int id) {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            PantryItem item = ds.getPantryItem(id);
            ds.close();

            if (item == null) {
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            editName.setText(item.getName());
            editQuantity.setText(formatQuantity(item.getQuantity()));
            editExpiry.setText(item.getExpiryDate());
            selectUnit(item.getUnit());
        } catch (Exception e) {
            Toast.makeText(this, R.string.load_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /** Selects the matching unit in the spinner. */
    private void selectUnit(String unit) {
        String[] units = getResources().getStringArray(R.array.units);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equalsIgnoreCase(unit)) {
                spinnerUnit.setSelection(i);
                return;
            }
        }
    }

    /** Tapping the expiry field opens a DatePickerDialog; the chosen date is shown as yyyy-MM-dd. */
    private void initExpiryPicker() {
        editExpiry.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, day) -> editExpiry.setText(
                            String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        Button clear = findViewById(R.id.buttonClearExpiry);
        clear.setOnClickListener(v -> editExpiry.setText(""));
    }

    private void initButtons() {
        Button save = findViewById(R.id.buttonSave);
        Button cancel = findViewById(R.id.buttonCancel);
        save.setOnClickListener(v -> saveItem());
        cancel.setOnClickListener(v -> finish());   // finish() closes this screen and returns to the list
    }

    /**
     * Validates the form. Returns true only if every field is acceptable.
     * Problems are shown directly on the field with setError().
     */
    private boolean validateInput() {
        String name = editName.getText().toString().trim();
        String qtyText = editQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError(getString(R.string.error_name_required));
            editName.requestFocus();
            return false;
        }
        if (qtyText.isEmpty()) {
            editQuantity.setError(getString(R.string.error_quantity_required));
            editQuantity.requestFocus();
            return false;
        }
        try {
            double quantity = Double.parseDouble(qtyText);
            if (quantity <= 0) {
                editQuantity.setError(getString(R.string.error_quantity_positive));
                editQuantity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            // e.g. "." or "1.2.3" slips past the numeric keyboard
            editQuantity.setError(getString(R.string.error_quantity_invalid));
            editQuantity.requestFocus();
            return false;
        }
        return true;
    }

    /** Validates, then inserts (new) or updates (existing) the item and closes the screen. */
    private void saveItem() {
        if (!validateInput()) {
            return;   // stop here; the user sees the error message on the field
        }

        PantryItem item = new PantryItem();
        item.setId(itemId);   // -1 for a new item, real id when editing
        item.setName(editName.getText().toString().trim());
        item.setQuantity(Double.parseDouble(editQuantity.getText().toString().trim()));
        item.setUnit(spinnerUnit.getSelectedItem().toString());
        item.setExpiryDate(editExpiry.getText().toString().trim());

        PantryDataSource ds = new PantryDataSource(this);
        boolean didSucceed;
        try {
            ds.open();
            if (itemId == -1) {
                didSucceed = ds.insertPantryItem(item);   // CREATE
            } else {
                didSucceed = ds.updatePantryItem(item);   // UPDATE
            }
            ds.close();
        } catch (Exception e) {
            didSucceed = false;
        }

        if (didSucceed) {
            Toast.makeText(this,
                    itemId == -1 ? R.string.item_saved : R.string.item_updated,
                    Toast.LENGTH_SHORT).show();
            finish();   // back to the list; its onResume() reloads the data
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
        }
    }

    /** Shows 3.0 as "3" and keeps 0.5 as "0.5". */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}