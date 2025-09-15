package com.kosala.pizza_mania;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng[] route = new LatLng[]{
            new LatLng(6.9271, 79.8612), // Colombo
            new LatLng(6.7132, 79.9026), // Panadura
            new LatLng(6.5854, 79.9607), // Kalutara
            new LatLng(6.4788, 80.0005), // Beruwala
            new LatLng(6.1406, 80.1010), // Hikkaduwa
            new LatLng(6.0535, 80.2210)  // Galle
    };

    private String[] cityNames = {"Colombo","Panadura","Kalutara","Beruwala","Hikkaduwa","Galle"};
    private int currentIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        for (int i = 0; i < route.length; i++) {
            mMap.addMarker(new MarkerOptions().position(route[i]).title(cityNames[i]));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route[0], 8));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng clicked = marker.getPosition();

                if (clicked.equals(route[currentIndex])) {
                    Toast.makeText(getContext(), "✅ Correct: " + cityNames[currentIndex], Toast.LENGTH_SHORT).show();
                    currentIndex++;
                    if (currentIndex == route.length) {
                        Toast.makeText(getContext(), "🎉 You completed the route!", Toast.LENGTH_LONG).show();
                        currentIndex = 0;
                    }
                } else {
                    Toast.makeText(getContext(), "❌ Wrong Way! Start again.", Toast.LENGTH_SHORT).show();
                    currentIndex = 0;
                }
                return false;
            }
        });
    }
}
