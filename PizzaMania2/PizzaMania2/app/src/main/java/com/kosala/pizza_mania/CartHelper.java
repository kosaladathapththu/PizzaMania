package com.kosala.pizza_mania;

import com.kosala.pizza_mania.models.Pizza;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartHelper {

    // Map pizza name -> Pizza object
    private static final Map<String, Pizza> pizzaMap = new HashMap<>();
    // Map pizza name -> quantity
    private static final Map<String, Integer> quantityMap = new HashMap<>();

    public static void addToCart(Pizza pizza) {
        pizzaMap.put(pizza.getName(), pizza);
        quantityMap.put(pizza.getName(), quantityMap.getOrDefault(pizza.getName(), 0) + 1);
    }

    public static void removeOneFromCart(Pizza pizza) {
        String name = pizza.getName();
        if (quantityMap.containsKey(name)) {
            int qty = quantityMap.get(name);
            if (qty <= 1) {
                quantityMap.remove(name);
                pizzaMap.remove(name);
            } else {
                quantityMap.put(name, qty - 1);
            }
        }
    }

    public static void removeAllFromCart(Pizza pizza) {
        quantityMap.remove(pizza.getName());
        pizzaMap.remove(pizza.getName());
    }

    public static int getQuantity(Pizza pizza) {
        return quantityMap.getOrDefault(pizza.getName(), 0);
    }

    public static List<Pizza> getCartItems() {
        return new ArrayList<>(pizzaMap.values());
    }

    public static boolean isEmpty() {
        return pizzaMap.isEmpty();
    }
}
