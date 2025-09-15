package com.kosala.pizza_mania;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private UsersAdapter adapter;
    private ArrayList<UserModel> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        adapter = new UsersAdapter(userList);
        recyclerUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error: " + e.getMessage()));
    }
}
