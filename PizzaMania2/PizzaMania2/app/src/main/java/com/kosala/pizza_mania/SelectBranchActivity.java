package com.kosala.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kosala.pizza_mania.adapters.BranchAdapter;
import com.kosala.pizza_mania.models.Branch;

import java.util.ArrayList;
import java.util.List;

public class SelectBranchActivity extends AppCompatActivity implements BranchAdapter.OnBranchClickListener {

    private RecyclerView rvBranches;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private List<Branch> branchList;
    private BranchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_branch);

        rvBranches = findViewById(R.id.rvBranches);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        branchList = new ArrayList<>();
        adapter = new BranchAdapter(branchList, this);
        rvBranches.setLayoutManager(new LinearLayoutManager(this));
        rvBranches.setAdapter(adapter);

        loadBranches();
    }

    private void loadBranches() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("branches")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        branchList.clear();
                        QuerySnapshot snapshot = task.getResult();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Branch branch = doc.toObject(Branch.class);
                            if (branch != null) {
                                branch.setId(doc.getId());
                                branchList.add(branch);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load branches", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBranchClick(Branch branch) {
        // pass branchId to AddPizzaActivity
        Intent intent = new Intent(this, AddPizzaActivity.class);
        intent.putExtra("branchId", branch.getId());
        startActivity(intent);
    }
}
