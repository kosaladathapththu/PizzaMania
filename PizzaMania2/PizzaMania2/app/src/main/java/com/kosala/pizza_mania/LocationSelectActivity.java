package com.kosala.pizza_mania;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Address;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kosala.pizza_mania.models.CartItem;
import com.kosala.pizza_mania.utils.CartDatabaseHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationSelectActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION_PERMISSION = 1001;

    private GoogleMap mMap;
    private Marker currentMarker;
    private LatLng selectedLatLng;
    private Button btnConfirm;
    private ImageButton btnMyLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private CartDatabaseHelper dbHelper;

    // Example branch location (TODO: fetch nearest from Firestore dynamically)
    private double branchLat = 6.9271; // Colombo
    private double branchLng = 79.8612;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        dbHelper = new CartDatabaseHelper(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commitAllowingStateLoss();

        mapFragment.getMapAsync(this);

        setupPlacesAutocomplete();

        // ✅ Confirm button → Save order to Firestore
        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Please choose a location!", Toast.LENGTH_SHORT).show();
                return;
            }

            saveOrderToFirestore();
        });

        btnMyLocation.setOnClickListener(v -> goToMyLocation());
    }

    private void setupPlacesAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
            autocompleteFragment.setHint("Search address or place...");

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null && mMap != null) {
                        moveCameraAndPlaceMarker(latLng, place.getAddress() != null ? place.getAddress() : place.getName());
                    }
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(LocationSelectActivity.this, "Place error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveOrderToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Get cart items and total
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        double totalPrice = CartManager.getInstance().getTotal();

        // 🔄 Convert CartItems to Firestore-friendly maps
        List<Map<String, Object>> itemsList = new java.util.ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            itemsList.add(map);
        }

        // Build order data
        Map<String, Object> order = new HashMap<>();
        String customerId = FirebaseAuth.getInstance().getUid(); // 🔹 real Firebase user ID
        if (customerId == null) customerId = "guest"; // fallback
        order.put("customerId", customerId);
        order.put("items", itemsList);
        order.put("totalPrice", totalPrice);

        Map<String, Object> deliveryLocation = new HashMap<>();
        deliveryLocation.put("lat", selectedLatLng.latitude);
        deliveryLocation.put("lng", selectedLatLng.longitude);
        deliveryLocation.put("address", getAddressFromLatLng(selectedLatLng));
        order.put("deliveryLocation", deliveryLocation);

        // 🔹 TODO: dynamic nearest branch, for now hardcoded
        order.put("branchId", "Colombo Branch");
        order.put("status", "pending");
        order.put("createdAt", FieldValue.serverTimestamp());

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    // ✅ Clear cart
                    CartManager.getInstance().clearCart();

                    Toast.makeText(this, "Order placed successfully! ✅", Toast.LENGTH_LONG).show();

                    // Go to Delivery screen
                    Intent i = new Intent(LocationSelectActivity.this, DeliveryProgressActivity.class);
                    i.putExtra("customer_lat", selectedLatLng.latitude);
                    i.putExtra("customer_lng", selectedLatLng.longitude);
                    i.putExtra("branch_lat", branchLat);
                    i.putExtra("branch_lng", branchLng);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to place order ❌", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndCenter();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
        }

        mMap.setOnMapClickListener(latLng -> moveCameraAndPlaceMarker(latLng, "Selected Location"));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(Marker marker) {}
            @Override public void onMarkerDrag(Marker marker) {}
            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLatLng = marker.getPosition();
                String addr = getAddressFromLatLng(selectedLatLng);
                if (addr != null && !addr.isEmpty()) {
                    marker.setTitle(addr);
                    marker.showInfoWindow();
                }
            }
        });
    }

    private void enableMyLocationAndCenter() {
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        goToMyLocation();
    }

    private void goToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && mMap != null) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                moveCameraAndPlaceMarker(loc, "Your Location");
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Location error", Toast.LENGTH_SHORT).show()
        );
    }

    private void moveCameraAndPlaceMarker(LatLng latLng, String title) {
        if (mMap == null || latLng == null) return;

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
        selectedLatLng = latLng;

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions mo = new MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(true);

        currentMarker = mMap.addMarker(mo);
        if (currentMarker != null) currentMarker.showInfoWindow();
    }

    private String getAddressFromLatLng(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (list != null && !list.isEmpty()) {
                Address addr = list.get(0);
                StringBuilder out = new StringBuilder();
                if (addr.getThoroughfare() != null) out.append(addr.getThoroughfare()).append(" ");
                if (addr.getLocality() != null) out.append(addr.getLocality()).append(" ");
                if (addr.getCountryName() != null) out.append(addr.getCountryName());
                return out.toString().trim();
            }
        } catch (IOException ignored) { }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    enableMyLocationAndCenter();
                }
            } else {
                Toast.makeText(this, "Location permission required to show your position.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
