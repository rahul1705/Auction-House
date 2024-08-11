package com.rsdevelopers.auctionhub.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.Models.OnItemClickListener;
import com.rsdevelopers.auctionhub.R;

import java.util.List;

public class MyItemsAdapter extends RecyclerView.Adapter<MyItemsAdapter.ItemsViewHolder> {

    private final List<AuctionItem> mDataList;
    private OnItemClickListener mListener;
    private final Context mContext;

    public MyItemsAdapter(Context mContext, List<AuctionItem> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mylist_items, parent, false);
        return new ItemsViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        AuctionItem items = mDataList.get(position);
        holder.bind(mContext, items);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {

        ImageView iImage;
        TextView iName, iStatus;

        public ItemsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            iName = itemView.findViewById(R.id.mylist_item_name);
            iImage = itemView.findViewById(R.id.mylist_item_img);
            iStatus = itemView.findViewById(R.id.item_status_mylist);

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        public void bind(Context mContext, AuctionItem items) {
            iName.setText(items.getItemName());
            iStatus.setText(items.getItemStatus());
            Glide.with(mContext).load(items.getItemImage()).into(iImage);
        }
    }

}
