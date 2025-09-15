package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnSignup;
    private TextView btnGoToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> {
            if (validateInputs()) registerUser();
        });

        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
            tilPassword.setError("Password should contain letters and numbers");
            isValid = false;
        }

        return isValid;
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendVerificationEmail(user);
                            saveUserToFirestore(user.getUid(), email);
                        }
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        handleRegistrationError(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserToFirestore(String uid, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("role", "customer"); // Default role

        db.collection("users")
                .document(uid)
                .set(userData);
    }

    private void handleRegistrationError(Exception exception) {
        String msg = "Registration failed";
        if (exception != null && exception.getMessage() != null) {
            String exMsg = exception.getMessage();
            if (exMsg.contains("email address is already in use")) {
                tilEmail.setError("Email already in use");
                msg = "This email is already registered.";
            } else if (exMsg.contains("weak password")) {
                tilPassword.setError("Password is too weak");
                msg = "Please choose a stronger password";
            } else msg = exMsg;
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Toast.makeText(this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSignup.setEnabled(!show);
        btnSignup.setText(show ? "Creating Account..." : "Create Account");
    }
}
