package com.kosala.pizza_mania;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnMyOrders = view.findViewById(R.id.btnMyOrders); // 🔹 new button

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔹 My Orders
        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyOrdersActivity.class);
            startActivity(intent);
        });

        // 🔹 Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            View dialogView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.dialog_edit_profile, null);

            EditText etName = dialogView.findViewById(R.id.etName);
            EditText etTel = dialogView.findViewById(R.id.etTel);
            EditText etAddress = dialogView.findViewById(R.id.etAddress);

            // Load current profile
            db.collection("users").document(auth.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            etName.setText(snapshot.getString("name"));
                            etTel.setText(snapshot.getString("tel"));
                            etAddress.setText(snapshot.getString("address"));
                        }
                    });

            new AlertDialog.Builder(getActivity())
                    .setTitle("Edit Profile")
                    .setView(dialogView)
                    .setPositiveButton("Save", (d, which) -> {
                        String name = etName.getText().toString().trim();
                        String tel = etTel.getText().toString().trim();
                        String address = etAddress.getText().toString().trim();

                        if (name.isEmpty() || tel.isEmpty() || address.isEmpty()) {
                            Toast.makeText(getActivity(), "All fields required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", name);
                        updates.put("tel", tel);
                        updates.put("address", address);

                        DocumentReference userRef = db.collection("users").document(auth.getUid());
                        userRef.update(updates)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(),
                                        "Profile updated ✅", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getActivity(),
                                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 🔹 Change Password
        btnChangePassword.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = auth.getCurrentUser().getEmail();
            if (email != null) {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(),
                                        "Password reset email sent to " + email,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(),
                                        "Failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // 🔹 Logout
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
