package com.kosala.pizza_mania;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPizzaActivity extends AppCompatActivity {

    private EditText etName, etPrice, etImageUrl;
    private Button btnAddPizza;

    private FirebaseFirestore db;
    private String branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pizza);

        // Link EditTexts from XML
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etImageUrl = findViewById(R.id.etImageUrl); // <-- fixed

        // Link Button from XML
        btnAddPizza = findViewById(R.id.btnAddPizza);

        db = FirebaseFirestore.getInstance();
        branchId = getIntent().getStringExtra("branchId");

        btnAddPizza.setOnClickListener(v -> uploadPizza());
    }

    private void uploadPizza() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Pizza...");
        progressDialog.show();

        Map<String, Object> pizza = new HashMap<>();
        pizza.put("name", name);
        pizza.put("price", price);
        pizza.put("imageUrl", imageUrl);

        db.collection("branches")
                .document(branchId)
                .collection("menu")
                .add(pizza)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Pizza Added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
