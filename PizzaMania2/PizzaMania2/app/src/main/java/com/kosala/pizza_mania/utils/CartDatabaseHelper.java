package com.kosala.pizza_mania.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kosala.pizza_mania.models.CartItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pizza_cart.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CART = "cart";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_QUANTITY = "quantity";

    private final Context context;

    public CartDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT UNIQUE, "
                + COLUMN_PRICE + " REAL, "
                + COLUMN_QUANTITY + " INTEGER)";
        db.execSQL(CREATE_CART_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    // --- SQLite CRUD Methods ---
    public void addToCart(CartItem item) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_CART,
                new String[]{COLUMN_QUANTITY},
                COLUMN_NAME + "=?",
                new String[]{item.getName()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int oldQty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, oldQty + item.getQuantity());
            db.update(TABLE_CART, values, COLUMN_NAME + "=?", new String[]{item.getName()});
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, item.getName());
            values.put(COLUMN_PRICE, item.getPrice());
            values.put(COLUMN_QUANTITY, item.getQuantity());
            db.insert(TABLE_CART, null, values);
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    public List<CartItem> getAllCartItems() {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CART, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
                list.add(new CartItem(name, price, quantity));
            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();
        db.close();
        return list;
    }

    public double getTotalPrice() {
        double total = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_PRICE + " * " + COLUMN_QUANTITY + ") FROM " + TABLE_CART, null);
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        if (cursor != null) cursor.close();
        db.close();
        return total;
    }

    public void clearCart() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, null, null);
        db.close();
    }

    // --- Firestore Integration ---
    public void pushCartToFirestore(@NonNull Map<String, Object> deliveryLocation,
                                    @NonNull String branchId,
                                    @NonNull Runnable onSuccess,
                                    @NonNull Runnable onFailure) {

        List<CartItem> cartItems = getAllCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Cart is empty ❌", Toast.LENGTH_SHORT).show();
            onFailure.run();
            return;
        }

        List<Map<String, Object>> itemsList = new ArrayList<>();
        double total = 0;

        for (CartItem item : cartItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            itemsList.add(map);
            total += item.getPrice() * item.getQuantity();
        }

        Map<String, Object> order = new HashMap<>();
        String customerId = FirebaseAuth.getInstance().getUid();
        if (customerId == null) customerId = "guest";

        order.put("customerId", customerId);
        order.put("items", itemsList);
        order.put("totalPrice", total);
        order.put("deliveryLocation", deliveryLocation);
        order.put("branchId", branchId);
        order.put("status", "pending");
        order.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    clearCart();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> onFailure.run());
    }
}
