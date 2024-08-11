package com.rsdevelopers.auctionhub.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rsdevelopers.auctionhub.Models.Transactions;
import com.rsdevelopers.auctionhub.R;

import java.util.ArrayList;

public class TransAdapter extends RecyclerView.Adapter<TransAdapter.TransViewHolder> {

    Context context;
    ArrayList<Transactions> arrayList;

    public TransAdapter(Context context, ArrayList<Transactions> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public TransAdapter.TransViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.wallet_items, parent, false);
        return new TransViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransAdapter.TransViewHolder holder, int position) {
        Transactions transactions = arrayList.get(position);
        String amount = "₹" + transactions.getTransAmount();
        holder.trans_amount.setText(amount);
        holder.trans_time.setText(String.valueOf(transactions.getTransDate()));
        holder.trans_reason.setText(transactions.getTransReason());

        if (transactions.getTransType().equals("cr")) {
            holder.trans_amount.setTextColor(holder.trans_amount.getContext().getColor(R.color.green));
        } else {
            holder.trans_amount.setTextColor(holder.trans_amount.getContext().getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class TransViewHolder extends RecyclerView.ViewHolder {

        TextView trans_time, trans_amount, trans_reason;

        public TransViewHolder(@NonNull View itemView) {
            super(itemView);
            trans_amount = itemView.findViewById(R.id.trans_amount);
            trans_time = itemView.findViewById(R.id.trans_time);
            trans_reason = itemView.findViewById(R.id.trans_reason);

        }
    }
}
