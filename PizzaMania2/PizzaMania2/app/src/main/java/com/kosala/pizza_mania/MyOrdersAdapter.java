package com.kosala.pizza_mania;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kosala.pizza_mania.models.Order;
import com.kosala.pizza_mania.models.Pizza;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.ViewHolder> {

    private final List<Order> orderList;

    public MyOrdersAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Show order items
        StringBuilder itemsText = new StringBuilder();
        for (Pizza pizza : order.getItems()) {
            itemsText.append(pizza.getName())
                    .append(" x")
                    .append(pizza.getQuantity())
                    .append(" - Rs.")
                    .append(pizza.getPrice())
                    .append("\n");
        }

        holder.tvItems.setText(itemsText.toString().trim());
        holder.tvStatus.setText("Status: " + order.getStatus());
        holder.tvBranch.setText("Branch: " + order.getBranchName());

        // Review button click
        holder.btnAddReview.setOnClickListener(v ->
                showReviewDialog(holder.itemView.getContext(), order.getOrderId())
        );
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Show dialog to add review
    private void showReviewDialog(Context context, String orderId) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_review, null);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);

        new AlertDialog.Builder(context)
                .setTitle("Add Review")
                .setView(view)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();
                    saveReviewToFirestore(orderId, rating, comment, context);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Save review to Firestore
    private void saveReviewToFirestore(String orderId, float rating, String comment, Context context) {
        String customerId = FirebaseAuth.getInstance().getUid();
        if (customerId == null) customerId = "guest";

        Map<String, Object> review = new HashMap<>();
        review.put("orderId", orderId);
        review.put("customerId", customerId);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(docRef ->
                        Toast.makeText(context, "Review submitted! ✅", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to submit review ❌", Toast.LENGTH_SHORT).show()
                );
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItems, tvStatus, tvBranch;
        Button btnAddReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBranch = itemView.findViewById(R.id.tvBranch);
            btnAddReview = itemView.findViewById(R.id.btnAddReview);
        }
    }
}
