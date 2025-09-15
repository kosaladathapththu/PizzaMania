package com.kosala.pizza_mania;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private final List<OrderModel> orders;
    private final FirebaseFirestore db; // 🔥 Firestore instance

    public OrdersAdapter(List<OrderModel> orders) {
        this.orders = orders;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);

        holder.tvOrderId.setText("Order: " + order.getOrderId());
        holder.tvTotal.setText("Total: Rs. " + order.getTotalPrice());
        holder.tvStatus.setText("Status: " + order.getStatus());

        // 🟢 Fetch customer name from "users" collection
        if (order.getCustomerId() != null && !order.getCustomerId().isEmpty()) {
            db.collection("users")
                    .document(order.getCustomerId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String customerName = documentSnapshot.getString("name");
                            if (customerName != null) {
                                holder.tvCustomer.setText("Customer: " + customerName);
                            } else {
                                holder.tvCustomer.setText("Customer: [No Name]");
                            }
                        } else {
                            holder.tvCustomer.setText("Customer: [Not Found]");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.tvCustomer.setText("Customer: [Error]");
                    });
        } else {
            holder.tvCustomer.setText("Customer: [Unknown]");
        }

        // 🔄 Update button listener
        holder.btnUpdate.setOnClickListener(v -> {
            showStatusDialog(holder, order, position);
        });
    }

    private void showStatusDialog(OrderViewHolder holder, OrderModel order, int position) {
        String[] statuses = {"Pending", "Preparing", "Delivered", "Completed"};

        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Update Order Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];

                    db.collection("orders")
                            .document(order.getOrderId())
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                order.setStatus(newStatus);
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e -> {
                                // optional: show toast/log
                            });
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomer, tvTotal, tvStatus;
        Button btnUpdate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
        }
    }
}
