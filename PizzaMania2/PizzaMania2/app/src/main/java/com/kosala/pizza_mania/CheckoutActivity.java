package com.kosala.pizza_mania;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kosala.pizza_mania.models.Branch;
import com.kosala.pizza_mania.utils.CartDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RadioButton rbCOD, rbOnline, rbCard;
    private RadioGroup rgPayment;
    private MaterialCardView cardCOD, cardOnline, cardCardPayment;
    private LinearLayout llCardDetails;
    private EditText etCardName, etCardNumber, etExpiry, etCVV;
    private Button btnDelivery, btnPickup;
    private CartDatabaseHelper dbHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();

        rgPayment = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbOnline = findViewById(R.id.rbOnline);
        rbCard = findViewById(R.id.rbCard);

        cardCOD = findViewById(R.id.cardCOD);
        cardOnline = findViewById(R.id.cardOnline);
        cardCardPayment = findViewById(R.id.cardCardPayment);

        llCardDetails = findViewById(R.id.llCardDetails);
        etCardName = findViewById(R.id.etCardName);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCVV = findViewById(R.id.etCVV);

        btnDelivery = findViewById(R.id.btnDelivery);
        btnPickup = findViewById(R.id.btnPickup);

        dbHelper = new CartDatabaseHelper(this);

        // Click listeners for payment methods
        cardCOD.setOnClickListener(v -> rgPayment.check(R.id.rbCOD));
        cardOnline.setOnClickListener(v -> rgPayment.check(R.id.rbOnline));
        cardCardPayment.setOnClickListener(v -> rgPayment.check(R.id.rbCard));

        rbCOD.setOnClickListener(v -> rgPayment.check(R.id.rbCOD));
        rbOnline.setOnClickListener(v -> rgPayment.check(R.id.rbOnline));
        rbCard.setOnClickListener(v -> rgPayment.check(R.id.rbCard));

        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            boolean showCardDetails = checkedId == R.id.rbCard;
            llCardDetails.setVisibility(showCardDetails ? View.VISIBLE : View.GONE);

            int highlightColor = Color.parseColor("#FF5722");
            setCardHighlight(cardCOD, checkedId == R.id.rbCOD, highlightColor);
            setCardHighlight(cardOnline, checkedId == R.id.rbOnline, highlightColor);
            setCardHighlight(cardCardPayment, checkedId == R.id.rbCard, highlightColor);
        });

        btnDelivery.setOnClickListener(v -> processDelivery());
        btnPickup.setOnClickListener(v -> processPickup());
    }

    private void setCardHighlight(MaterialCardView card, boolean checked, int color) {
        if (card == null) return;
        if (checked) {
            card.setStrokeWidth(dpToPx(2));
            card.setStrokeColor(color);
        } else {
            card.setStrokeWidth(0);
            card.setStrokeColor(Color.TRANSPARENT);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void processDelivery() {
        int checkedId = rgPayment.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Select payment method!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkedId == R.id.rbCard) {
            String name = etCardName.getText().toString().trim();
            String card = etCardNumber.getText().toString().trim().replaceAll("\\s+", "");
            String expiry = etExpiry.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

            if (name.isEmpty() || card.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Enter all card details!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Open delivery location screen
        Intent intent = new Intent(this, LocationSelectActivity.class);
        startActivity(intent);
    }

    private void processPickup() {
        // ✅ Open Google Maps to nearest branch
        db.collection("branches")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Branch nearest = null;
                    if (!querySnapshot.isEmpty()) {
                        List<Branch> branches = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Branch b = new Branch();
                            b.setId(doc.getId());
                            b.setName(doc.getString("name"));
                            Double lat = doc.getDouble("lat");
                            Double lng = doc.getDouble("lng");
                            if (lat != null && lng != null) {
                                b.setLat(lat);
                                b.setLng(lng);
                                branches.add(b);
                            }
                        }

                        if (!branches.isEmpty()) nearest = branches.get(0);
                    }

                    if (nearest == null) {
                        nearest = new Branch("Colombo Branch", 6.9271, 79.8612);
                    }

                    String uri = "https://www.google.com/maps/dir/?api=1&destination="
                            + nearest.getLat() + "," + nearest.getLng()
                            + "&travelmode=driving";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {
                    Branch fallback = new Branch("Colombo Branch", 6.9271, 79.8612);
                    String uri = "https://www.google.com/maps/dir/?api=1&destination="
                            + fallback.getLat() + "," + fallback.getLng()
                            + "&travelmode=driving";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                });
    }
}
