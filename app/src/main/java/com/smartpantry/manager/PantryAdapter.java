package com.smartpantry.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Connects the list of PantryItem objects to the RecyclerView.
 * It creates one row view per item, fills it with data, and handles
 * the Delete button shown when delete mode is switched on.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final ArrayList<PantryItem> pantryData;   // the data being displayed
    private final Context parentContext;              // needed for database access and dialogs
    private View.OnClickListener onItemClickListener; // set by the Activity (used later for editing)
    private Runnable onDataChanged;                   // tells the Activity to refresh its empty message
    private boolean isDeleting = false;               // true while the Delete switch is on

    /** Constructor: receives the data and the Activity this list lives in. */
    public PantryAdapter(ArrayList<PantryItem> data, Context context) {
        this.pantryData = data;
        this.parentContext = context;
    }

    /**
     * ViewHolder: holds references to the widgets in ONE row so Android
     * does not have to call findViewById every time the row is reused.
     */
    public static class PantryViewHolder extends RecyclerView.ViewHolder {
        public final TextView textName;
        public final TextView textQuantity;
        public final TextView textExpiry;
        public final Button buttonDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            textExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteItem);
        }
    }

    /** Lets the Activity supply the code that runs when a row is tapped. */
    public void setOnItemClickListener(View.OnClickListener listener) {
        onItemClickListener = listener;
    }

    /** Lets the Activity run code whenever the list changes (e.g. after a delete). */
    public void setOnDataChanged(Runnable callback) {
        onDataChanged = callback;
    }

    /** Switches delete mode on or off and redraws the list so Delete buttons appear/disappear. */
    public void setDeleting(boolean deleting) {
        isDeleting = deleting;
        notifyDataSetChanged();
    }

    /** Called when RecyclerView needs a new row: inflate item_pantry.xml into a ViewHolder. */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(v);
    }

    /** Called for each visible row: copy the item's data into the row's widgets. */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryData.get(position);

        holder.textName.setText(item.getName());
        holder.textQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        // The expiry date is optional, so hide the line if there isn't one
        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.textExpiry.setVisibility(View.GONE);
        } else {
            holder.textExpiry.setVisibility(View.VISIBLE);
            holder.textExpiry.setText(parentContext.getString(R.string.expires_on, expiry));
        }

        // Tapping the row: tag the row with its ViewHolder so the Activity can find the position
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(onItemClickListener);

        // Delete button is only visible in delete mode
        if (isDeleting) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    confirmDelete(pos);
                }
            });
        } else {
            holder.buttonDelete.setVisibility(View.GONE);
        }
    }

    /** Tells RecyclerView how many rows there are. */
    @Override
    public int getItemCount() {
        return pantryData.size();
    }

    /** Shows a confirmation dialog so an ingredient is not deleted by accident. */
    private void confirmDelete(int position) {
        PantryItem item = pantryData.get(position);
        new AlertDialog.Builder(parentContext)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(parentContext.getString(R.string.confirm_delete_message, item.getName()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteItem(position))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /** Deletes the item from the database, then from the list, then refreshes the display. */
    private void deleteItem(int position) {
        PantryItem item = pantryData.get(position);
        PantryDataSource ds = new PantryDataSource(parentContext);
        try {
            ds.open();
            boolean didDelete = ds.deletePantryItem(item.getId());
            ds.close();
            if (didDelete) {
                pantryData.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(parentContext, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } else {
                Toast.makeText(parentContext, R.string.delete_failed, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(parentContext, R.string.delete_failed, Toast.LENGTH_LONG).show();
        }
    }

    /** Shows 3.0 as "3" but keeps 0.5 as "0.5". */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}