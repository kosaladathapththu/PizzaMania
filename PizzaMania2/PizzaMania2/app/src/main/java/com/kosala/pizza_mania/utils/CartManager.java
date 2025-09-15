package com.kosala.pizza_mania.utils;

import com.kosala.pizza_mania.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(CartItem item) {
        // check if already exists → increase qty
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartItems.add(item);
    }

    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
