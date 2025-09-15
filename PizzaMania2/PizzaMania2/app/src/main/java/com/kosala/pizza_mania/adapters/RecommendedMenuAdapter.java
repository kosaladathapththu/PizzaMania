package com.kosala.pizza_mania.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kosala.pizza_mania.R;
import com.kosala.pizza_mania.models.Pizza;
import com.kosala.pizza_mania.models.CartItem;

import java.util.List;

public class RecommendedMenuAdapter extends RecyclerView.Adapter<RecommendedMenuAdapter.ViewHolder> {

    private final Context context;
    private final List<Pizza> pizzaList;
    private final FirebaseFirestore db;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Pizza pizza);
    }

    public RecommendedMenuAdapter(Context context, List<Pizza> pizzaList) {
        this.context = context;
        this.pizzaList = pizzaList;
        this.db = FirebaseFirestore.getInstance();
    }

    public RecommendedMenuAdapter(Context context, List<Pizza> pizzaList, OnItemClickListener listener) {
        this.context = context;
        this.pizzaList = pizzaList;
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pizza_with_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pizza pizza = pizzaList.get(position);

        holder.tvName.setText(pizza.getName());
        holder.tvPrice.setText("Rs " + pizza.getPrice());
        holder.tvQuantity.setText("1");

        // Load image
        String url = pizza.getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(context).load(url).placeholder(R.drawable.placeholder).into(holder.ivPizza);
        } else {
            holder.ivPizza.setImageResource(R.drawable.placeholder);
        }

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(pizza);
        });

        // Visual touch feedback
        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.8f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        });

        // Quantity buttons
        holder.btnIncrease.setOnClickListener(v -> {
            int qty = Integer.parseInt(holder.tvQuantity.getText().toString());
            holder.tvQuantity.setText(String.valueOf(qty + 1));
        });
        holder.btnDecrease.setOnClickListener(v -> {
            int qty = Integer.parseInt(holder.tvQuantity.getText().toString());
            if (qty > 1) holder.tvQuantity.setText(String.valueOf(qty - 1));
        });

        // Add to Firestore cart
        holder.btnAddToCart.setOnClickListener(v -> {
            int qty = Integer.parseInt(holder.tvQuantity.getText().toString());
            pizza.setQuantity(qty);

            CartItem cartItem = new CartItem(pizza.getName(), pizza.getPrice(), pizza.getQuantity());

            // Firestore add or update
            db.collection("cart")
                    .document(cartItem.getName()) // Using name as ID
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            // Already in cart → update quantity
                            Long existingQty = doc.getLong("quantity");
                            if (existingQty != null) {
                                cartItem.setQuantity(cartItem.getQuantity() + existingQty.intValue());
                            }
                        }
                        // Save updated quantity
                        db.collection("cart").document(cartItem.getName())
                                .set(cartItem)
                                .addOnSuccessListener(aVoid -> Toast.makeText(context,
                                        cartItem.getName() + " x" + cartItem.getQuantity() + " added to cart!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context,
                                        "Failed to add to cart!", Toast.LENGTH_SHORT).show());
                    });
        });
    }

    @Override
    public int getItemCount() {
        return pizzaList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPizza;
        TextView tvName, tvPrice, tvQuantity;
        Button btnAddToCart, btnIncrease, btnDecrease;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPizza = itemView.findViewById(R.id.ivPizza);
            tvName = itemView.findViewById(R.id.tvPizzaName);
            tvPrice = itemView.findViewById(R.id.tvPizzaPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}
