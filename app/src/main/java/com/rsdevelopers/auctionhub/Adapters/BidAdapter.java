package com.rsdevelopers.auctionhub.Adapters;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.Models.OnItemClickListener;
import com.rsdevelopers.auctionhub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BidAdapter extends RecyclerView.Adapter<BidAdapter.MyViewHolder> {

    private final List<AuctionItem> mDataList;
    private OnItemClickListener mListener;
    private final Context mContext;

    public BidAdapter(Context context, List<AuctionItem> dataList) {
        mContext = context;
        mDataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bid_list_item, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView iName, iExpiry;
        public ImageView iImage;
        public CountDownTimer mCountDownTimer;

        public MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            iName = itemView.findViewById(R.id.item_name_display);
            iExpiry = itemView.findViewById(R.id.expire_time_rv);
            iImage = itemView.findViewById(R.id.item_image_rv);

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

            // Cancel any previous timer before starting a new one
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            if (!items.getExpiryDate().equals("Expired")) {
                runTimer(items);
            }
            Glide.with(mContext).load(items.getItemImage()).into(iImage);
        }

        private void runTimer(AuctionItem items) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date expiryDate;
            try {
                expiryDate = format.parse(items.getExpiryDate());
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(itemView.getContext(), "No valid Date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (expiryDate != null) {
                long millisUntilFinished = expiryDate.getTime() - System.currentTimeMillis();

                mCountDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
                    public void onTick(long millisUntilFinished1) {
                        // Update UI with the remaining time
                        long days = millisUntilFinished1 / (1000 * 60 * 60 * 24);
                        long hours = (millisUntilFinished1 / (1000 * 60 * 60)) % 24;
                        long minutes = (millisUntilFinished1 / (1000 * 60)) % 60;
                        long seconds = (millisUntilFinished1 / 1000) % 60;
                        String timeLeft = String.format(Locale.getDefault(), "%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
                        iExpiry.setText(timeLeft);
                    }

                    public void onFinish() {
                        // Handle the expiry of the auction item
//                        iExpiry.setText("Expired");
                        items.setExpiryDate("Expired");
                        if (items.getBuyerId() != null) {
                            items.setItemStatus("Sold");
                        } else {
                            items.setItemStatus("Expired Unsold");
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Items").document(items.getItemId()).update("expiryDate", "Expired");
                        if (items.getBuyerId() != null) {
                            db.collection("Items").document(items.getItemId()).update("itemStatus", "Sold");
                        } else {
                            db.collection("Items").document(items.getItemId()).update("itemStatus", "Unsold");
                        }
                    }
                }.start();
            }
        }
    }
}

