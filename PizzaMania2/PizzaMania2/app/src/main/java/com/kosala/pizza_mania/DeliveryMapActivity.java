package com.kosala.pizza_mania;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeliveryMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double customerLat, customerLng, branchLat, branchLng;
    private static final String TAG = "DeliveryMapActivity";

    private Button btnBackHome; // 🔹 Back to home button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_map);

        customerLat = getIntent().getDoubleExtra("customer_lat", 0);
        customerLng = getIntent().getDoubleExtra("customer_lng", 0);
        branchLat = getIntent().getDoubleExtra("branch_lat", 0);
        branchLng = getIntent().getDoubleExtra("branch_lng", 0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // 🔹 Back Button setup
        btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> {
            // Go back to MainActivity which shows HomeFragment
            Intent intent = new Intent(DeliveryMapActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng branch = new LatLng(branchLat, branchLng);
        LatLng customer = new LatLng(customerLat, customerLng);

        mMap.addMarker(new MarkerOptions().position(branch).title("Pizza Branch"));
        mMap.addMarker(new MarkerOptions().position(customer).title("Delivery Address"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(branch, 12f));

        fetchDirections(branch, customer);
    }

    private void fetchDirections(LatLng origin, LatLng destination) {
        String apiKey = getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving&key=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DeliveryMapActivity.this, "Directions API error", Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Directions API request failed: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(DeliveryMapActivity.this, "Failed to get directions", Toast.LENGTH_SHORT).show());
                    return;
                }

                String jsonData = response.body().string();
                try {
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray routes = json.getJSONArray("routes");

                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                        String points = overviewPolyline.getString("points");

                        List<LatLng> decodedPath = decodePoly(points);

                        runOnUiThread(() -> {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(10f)
                                    .color(Color.BLUE);

                            mMap.addPolyline(polylineOptions);
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "JSON parse error: ", e);
                }
            }
        });
    }

    // Polyline decoding
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
