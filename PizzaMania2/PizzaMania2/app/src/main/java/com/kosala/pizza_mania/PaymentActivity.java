package com.kosala.pizza_mania;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class PaymentActivity extends AppCompatActivity {

    private RadioButton rbCOD, rbOnline, rbCard;
    private LinearLayout layoutCardDetails;
    private EditText etCardNumber, etExpiry, etCVV;
    private Button btnPay;
    private MaterialCardView cardCOD, cardOnline, cardCardPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        rbCOD = findViewById(R.id.rbCOD);
        rbOnline = findViewById(R.id.rbOnline);
        rbCard = findViewById(R.id.rbCard);
        layoutCardDetails = findViewById(R.id.layoutCardDetails);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCVV = findViewById(R.id.etCVV);
        btnPay = findViewById(R.id.btnPay);

        cardCOD = findViewById(R.id.cardCOD);
        cardOnline = findViewById(R.id.cardOnline);
        cardCardPayment = findViewById(R.id.cardCardPayment);

        // Clickable cards
        cardCOD.setOnClickListener(v -> selectPaymentOption(rbCOD));
        cardOnline.setOnClickListener(v -> selectPaymentOption(rbOnline));
        cardCardPayment.setOnClickListener(v -> selectPaymentOption(rbCard));

        btnPay.setOnClickListener(v -> processPayment());
    }

    private void selectPaymentOption(RadioButton selected) {
        rbCOD.setChecked(false);
        rbOnline.setChecked(false);
        rbCard.setChecked(false);
        selected.setChecked(true);

        // Show card details only if card payment is selected
        layoutCardDetails.setVisibility(selected == rbCard ? View.VISIBLE : View.GONE);
    }

    private void processPayment() {
        if (rbCOD.isChecked()) {
            Toast.makeText(this, "Order placed with Cash on Delivery", Toast.LENGTH_SHORT).show();
            finish();
        } else if (rbOnline.isChecked()) {
            Toast.makeText(this, "Online Payment Successful (Dummy)", Toast.LENGTH_SHORT).show();
            finish();
        } else if (rbCard.isChecked()) {
            String card = etCardNumber.getText().toString().trim();
            String exp = etExpiry.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

            if (card.isEmpty() || exp.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Please fill all card details", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Card Payment Successful (Dummy)", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
        }
    }
}
