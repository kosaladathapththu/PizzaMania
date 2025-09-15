package com.kosala.pizza_mania.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kosala.pizza_mania.R;
import com.kosala.pizza_mania.models.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartList;
    private final Runnable refreshCallback;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CartAdapter(Context context, List<CartItem> cartList, Runnable refreshCallback) {
        this.context = context;
        this.cartList = cartList;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText("Rs " + (item.getPrice() * item.getQuantity()));

        // Increase quantity
        holder.btnIncrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            db.collection("cart").document(item.getName())
                    .update("quantity", newQty)
                    .addOnSuccessListener(aVoid -> refreshCallback.run())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show());
        });

        // Decrease quantity
        holder.btnDecrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (newQty <= 0) {
                db.collection("cart").document(item.getName())
                        .delete()
                        .addOnSuccessListener(aVoid -> refreshCallback.run())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show());
            } else {
                db.collection("cart").document(item.getName())
                        .update("quantity", newQty)
                        .addOnSuccessListener(aVoid -> refreshCallback.run())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show());
            }
        });

        // Delete item
        holder.btnDelete.setOnClickListener(v -> {
            db.collection("cart").document(item.getName())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, item.getName() + " removed", Toast.LENGTH_SHORT).show();
                        refreshCallback.run();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;
        Button btnIncrease, btnDecrease, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPizzaName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPizzaPrice);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
