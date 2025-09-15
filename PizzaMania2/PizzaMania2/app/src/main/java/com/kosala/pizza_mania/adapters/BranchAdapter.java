package com.kosala.pizza_mania.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kosala.pizza_mania.R;
import com.kosala.pizza_mania.models.Branch;

import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    public interface OnBranchClickListener {
        void onBranchClick(Branch branch);
    }

    private List<Branch> branchList;
    private OnBranchClickListener listener;

    public BranchAdapter(List<Branch> branchList, OnBranchClickListener listener) {
        this.branchList = branchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);
        holder.tvName.setText(branch.getName());
        holder.itemView.setOnClickListener(v -> listener.onBranchClick(branch));
    }

    @Override
    public int getItemCount() {
        return branchList.size();
    }

    static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        BranchViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBranchName);
        }
    }
}
