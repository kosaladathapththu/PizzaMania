package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kosala.pizza_mania.adapters.CartAdapter;
import com.kosala.pizza_mania.models.CartItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // ✅ Initialize FirebaseAuth

        loadCartItemsFromFirestore();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Get current user ID or "guest"
            String customerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";

            // 1️⃣ Prepare order data
            List<Map<String, Object>> itemsList = new ArrayList<>();
            double totalPrice = 0;
            for (CartItem item : cartItems) {
                totalPrice += item.getPrice() * item.getQuantity();

                Map<String, Object> map = new HashMap<>();
                map.put("name", item.getName());
                map.put("price", item.getPrice());
                map.put("quantity", item.getQuantity());

                itemsList.add(map);
            }

            // 2️⃣ Create order map
            Map<String, Object> order = new HashMap<>();
            order.put("customerId", customerId); // ✅ Add customer ID
            order.put("items", itemsList);
            order.put("totalPrice", totalPrice);
            order.put("status", "pending"); // default status
            order.put("createdAt", FieldValue.serverTimestamp());

            // 3️⃣ Save order to Firestore
            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Order placed successfully! ✅", Toast.LENGTH_SHORT).show();

                        // Clear cart after placing order
                        clearCart();

                        // Go to CheckoutActivity (or you can go to DeliveryProgressActivity)
                        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadCartItemsFromFirestore() {
        cartItems.clear();

        db.collection("cart")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            Double price = doc.getDouble("price");
                            Long qty = doc.getLong("quantity");

                            if (name != null && price != null && qty != null) {
                                cartItems.add(new CartItem(name, price, qty.intValue()));
                            }
                        }

                        adapter = new CartAdapter(CartActivity.this, cartItems, this::loadCartItemsFromFirestore);
                        rvCart.setAdapter(adapter);

                        updateTotal();
                    } else {
                        Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotal.setText("Total: Rs " + total);
    }

    private void clearCart() {
        db.collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    cartItems.clear();
                    if (adapter != null) adapter.notifyDataSetChanged();
                    updateTotal();
                });
    }
}
