package com.kosala.pizza_mania;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private OrdersAdapter adapter;
    private List<OrderModel> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        recyclerView = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        fetchOrdersFromFirestore();
    }

    private void fetchOrdersFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("orders")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Map<String, Object> data = doc.getData();
                            orderList.add(new OrderModel(
                                    doc.getId(),
                                    (String) data.get("customerId"),
                                    (double) (data.get("totalPrice") != null ? (double) data.get("totalPrice") : 0),
                                    (String) data.get("status")
                            ));
                        }
                        adapter.notifyDataSetChanged();

                        tvEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Failed to load orders ❌");
                    }
                });
    }
}
