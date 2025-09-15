package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    public SettingsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
//        Button btnNotifications = view.findViewById(R.id.btnNotifications);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

            if (email != null) {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(),
                                        "Password reset email sent to " + email,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(),
                                        "Failed to send reset email: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();
            }
        });

//        // Notifications (placeholder)
//        btnNotifications.setOnClickListener(v ->
//                Toast.makeText(getActivity(), "Notification settings coming soon!", Toast.LENGTH_SHORT).show()
//        );

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
