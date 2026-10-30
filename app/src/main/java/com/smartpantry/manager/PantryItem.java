package com.smartpantry.manager;

/**
 * Represents one ingredient stored in the user's pantry.
 * Maps to one row in the pantry_item table.
 */
public class PantryItem {

    private int id;              // database primary key; -1 means "not saved yet"
    private String name;         // e.g. "tomatoes"
    private double quantity;     // e.g. 2.0
    private String unit;         // e.g. "kg", "g", "ml", "pcs"
    private String expiryDate;   // optional, stored as text e.g. "2026-11-15"; may be empty

    /** Creates a new, unsaved item. */
    public PantryItem() {
        this.id = -1;
        this.expiryDate = "";
    }

    /** Convenience constructor for building a complete item in one line. */
    public PantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}