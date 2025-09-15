package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kosala.pizza_mania.adapters.CartAdapter;
import com.kosala.pizza_mania.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        loadCartItemsFromFirestore();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            startActivity(intent);
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
}
