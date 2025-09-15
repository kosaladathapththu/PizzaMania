package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kosala.pizza_mania.adapters.CartAdapter;
import com.kosala.pizza_mania.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private FirebaseFirestore db;

    public CartFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = view.findViewById(R.id.rvCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        db = FirebaseFirestore.getInstance();

        loadCartItemsFromFirestore();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), CheckoutActivity.class);
            startActivity(intent);
        });

        return view;
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

                        adapter = new CartAdapter(requireContext(), cartItems, this::loadCartItemsFromFirestore);
                        rvCart.setAdapter(adapter);

                        updateTotal();
                    } else {
                        Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
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
