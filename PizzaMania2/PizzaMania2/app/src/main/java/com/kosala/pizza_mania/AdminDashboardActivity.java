package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CardView cardSignOut, cardManageUsers, cardManageOrders, cardManageMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        mAuth = FirebaseAuth.getInstance();

        // 🌟 Welcome message
        TextView tv = findViewById(R.id.tvAdminWelcome);
        tv.setText("Welcome Admin! You are logged in.");

        // 🎯 Get Cards
        cardSignOut = findViewById(R.id.cardSignOut);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageOrders = findViewById(R.id.cardManageOrders);
        cardManageMenu = findViewById(R.id.cardManageMenu);

        // 🖱️ Click Listeners
        cardSignOut.setOnClickListener(v -> signOutAdmin());

        cardManageUsers.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class))
        );

        cardManageOrders.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ManageOrdersActivity.class))
        );

        cardManageMenu.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ManageMenuActivity.class))
        );
    }

    private void signOutAdmin() {
        mAuth.signOut();
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
