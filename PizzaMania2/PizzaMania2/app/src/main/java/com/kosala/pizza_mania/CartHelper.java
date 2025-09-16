package com.kosala.pizza_mania;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kosala.pizza_mania.models.CartItem;
import com.kosala.pizza_mania.models.Pizza;
import com.kosala.pizza_mania.utils.CartDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartHelper {

    // In-memory cart
    private static final Map<String, Pizza> pizzaMap = new HashMap<>();
    private static final Map<String, Integer> quantityMap = new HashMap<>();

    // Database + Firestore
    private static CartDatabaseHelper dbHelper;
    private static FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static void init(Context context) {
        if (dbHelper == null) {
            dbHelper = new CartDatabaseHelper(context);
        }
    }

    // ✅ Add item (SQLite + Firestore + memory)
    public static void addToCart(Context context, Pizza pizza) {
        init(context);

        pizzaMap.put(pizza.getName(), pizza);
        int newQty = quantityMap.getOrDefault(pizza.getName(), 0) + 1;
        quantityMap.put(pizza.getName(), newQty);

        // Convert Pizza -> CartItem
        CartItem item = new CartItem(pizza.getName(), pizza.getPrice(), newQty);

        // Insert/Update in SQLite
        dbHelper.insertOrUpdateCartItem(item);

        // Insert/Update in Firestore
        firestore.collection("cart")
                .document(item.getName())
                .set(item)
                .addOnSuccessListener(aVoid -> Log.d("CartHelper", "Saved to Firestore"))
                .addOnFailureListener(e -> Log.e("CartHelper", "Firestore save failed", e));
    }

    // ✅ Remove one item
    public static void removeOneFromCart(Context context, Pizza pizza) {
        init(context);

        String name = pizza.getName();
        if (quantityMap.containsKey(name)) {
            int qty = quantityMap.get(name);
            if (qty <= 1) {
                quantityMap.remove(name);
                pizzaMap.remove(name);

                // Remove from SQLite + Firestore
                dbHelper.deleteCartItem(name);
                firestore.collection("cart").document(name).delete();
            } else {
                int newQty = qty - 1;
                quantityMap.put(name, newQty);

                CartItem item = new CartItem(name, pizza.getPrice(), newQty);

                dbHelper.insertOrUpdateCartItem(item);
                firestore.collection("cart").document(name).update("quantity", newQty);
            }
        }
    }

    // ✅ Remove all
    public static void removeAllFromCart(Context context, Pizza pizza) {
        init(context);

        String name = pizza.getName();
        quantityMap.remove(name);
        pizzaMap.remove(name);

        dbHelper.deleteCartItem(name);
        firestore.collection("cart").document(name).delete();
    }

    // ✅ Get quantity
    public static int getQuantity(Pizza pizza) {
        return quantityMap.getOrDefault(pizza.getName(), 0);
    }

    // ✅ Get all cart items
    public static List<CartItem> getCartItems(Context context) {
        init(context);
        return dbHelper.getAllCartItems(); // pull from SQLite
    }

    public static boolean isEmpty() {
        return pizzaMap.isEmpty();
    }
}
