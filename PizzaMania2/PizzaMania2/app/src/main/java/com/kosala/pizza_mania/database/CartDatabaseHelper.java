package com.kosala.pizza_mania.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kosala.pizza_mania.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pizza_cart.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CART = "cart";

    public CartDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CART +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, price REAL, quantity INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    // Insert or update cart item
    public void insertOrUpdateCartItem(CartItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE name=?", new String[]{item.getName()});
        if (cursor.moveToFirst()) {
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            ContentValues values = new ContentValues();
            values.put("quantity", qty + item.getQuantity());
            db.update(TABLE_CART, values, "name=?", new String[]{item.getName()});
        } else {
            ContentValues values = new ContentValues();
            values.put("name", item.getName());
            values.put("price", item.getPrice());
            values.put("quantity", item.getQuantity());
            db.insert(TABLE_CART, null, values);
        }
        cursor.close();
        db.close();
    }

    // Get all items
    public List<CartItem> getCartItems() {
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                items.add(new CartItem(name, price, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    // Delete item
    public void deleteItem(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, "name=?", new String[]{name});
        db.close();
    }

    // Update quantity
    public void updateQuantity(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        db.update(TABLE_CART, values, "name=?", new String[]{name});
        db.close();
    }
}
