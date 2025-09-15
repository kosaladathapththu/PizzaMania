package com.kosala.pizza_mania;

import com.kosala.pizza_mania.models.CartItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // Add item: if same name exists, increase qty
    public void addToCart(CartItem item) {
        for (CartItem ci : items) {
            if (ci.getName().equals(item.getName())) {
                ci.setQuantity(ci.getQuantity() + item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    public void updateQuantity(String name, int newQty) {
        if (newQty <= 0) {
            removeByName(name);
            return;
        }
        for (CartItem ci : items) {
            if (ci.getName().equals(name)) {
                ci.setQuantity(newQty);
                return;
            }
        }
    }

    public void removeByName(String name) {
        Iterator<CartItem> it = items.iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(name)) it.remove();
        }
    }

    public List<CartItem> getCartItems() {
        return items;
    }

    public double getTotal() {
        double total = 0;
        for (CartItem ci : items) total += ci.getPrice() * ci.getQuantity();
        return total;
    }

    public void clearCart() { items.clear(); }
}
