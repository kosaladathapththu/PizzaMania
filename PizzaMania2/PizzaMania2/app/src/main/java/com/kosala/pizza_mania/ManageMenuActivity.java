package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kosala.pizza_mania.adapters.MenuAdapter;
import com.kosala.pizza_mania.models.Branch;
import com.kosala.pizza_mania.models.Pizza;

import java.util.ArrayList;
import java.util.List;

public class ManageMenuActivity extends AppCompatActivity {

    private RecyclerView rvMenu;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private FirebaseFirestore db;
    private List<Pizza> pizzaList;
    private MenuAdapter adapter;
    private List<Branch> branchList;
    private Branch selectedBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        rvMenu = findViewById(R.id.rvMenu);
        progressBar = findViewById(R.id.progressBar);
        fabAdd = findViewById(R.id.fabAdd);

        db = FirebaseFirestore.getInstance();
        pizzaList = new ArrayList<>();
        branchList = new ArrayList<>();
        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Initialize adapter later after branch is selected
        adapter = new MenuAdapter(this, pizzaList, db, "");
        rvMenu.setAdapter(adapter);

        loadBranches(); // load branches and first branch menu automatically

        fabAdd.setOnClickListener(v -> {
            if (selectedBranch == null) {
                Toast.makeText(this, "Branches not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AddPizzaActivity.class);
            intent.putExtra("branchId", selectedBranch.getId());
            startActivity(intent);
        });
    }

    private void loadBranches() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("branches")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        branchList.clear();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Branch branch = doc.toObject(Branch.class);
                            if (branch != null) {
                                branch.setId(doc.getId());
                                branchList.add(branch);
                            }
                        }

                        if (!branchList.isEmpty()) {
                            // ✅ automatically select first branch
                            selectedBranch = branchList.get(0);
                            loadMenuForBranch(selectedBranch.getId());
                        } else {
                            Toast.makeText(this, "No branches found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load branches", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMenuForBranch(String branchId) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("branches")
                .document(branchId)
                .collection("menu")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        pizzaList.clear();
                        QuerySnapshot snapshot = task.getResult();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Pizza pizza = doc.toObject(Pizza.class);
                            if (pizza != null) {
                                pizza.setId(doc.getId());
                                pizzaList.add(pizza);
                            }
                        }

                        // ✅ re-init adapter with correct branchId
                        adapter = new MenuAdapter(this, pizzaList, db, branchId);
                        rvMenu.setAdapter(adapter);

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load menu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
