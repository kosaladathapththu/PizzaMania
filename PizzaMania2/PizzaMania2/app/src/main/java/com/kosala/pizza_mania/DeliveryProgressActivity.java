package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DeliveryProgressActivity extends AppCompatActivity {

    private ImageView gifView;
    private double customerLat, customerLng, branchLat, branchLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_progress);

        gifView = findViewById(R.id.gifView);

        // Get locations from intent
        customerLat = getIntent().getDoubleExtra("customer_lat", 0);
        customerLng = getIntent().getDoubleExtra("customer_lng", 0);
        branchLat = getIntent().getDoubleExtra("branch_lat", 0);
        branchLng = getIntent().getDoubleExtra("branch_lng", 0);

        // Step 1: show order processing
        Glide.with(this).asGif().load(R.drawable.process).into(gifView);

        Handler handler = new Handler();

        // Step 2: cooking
        handler.postDelayed(() -> {
            Glide.with(this).asGif().load(R.drawable.cooking).into(gifView);
        }, 3000);

        // Step 3: delivery
        handler.postDelayed(() -> {
            Glide.with(this).asGif().load(R.drawable.delivery).into(gifView);
        }, 6000);

        // Step 4: open DeliveryMapActivity with path
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, DeliveryMapActivity.class);
            intent.putExtra("customer_lat", customerLat);
            intent.putExtra("customer_lng", customerLng);
            intent.putExtra("branch_lat", branchLat);
            intent.putExtra("branch_lng", branchLng);
            startActivity(intent);
            finish();
        }, 9000);
    }
}
