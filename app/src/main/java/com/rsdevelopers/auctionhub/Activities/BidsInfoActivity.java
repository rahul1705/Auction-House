package com.rsdevelopers.auctionhub.Activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.Models.FirestoreOps;
import com.rsdevelopers.auctionhub.Models.Transactions;
import com.rsdevelopers.auctionhub.Models.Users;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.ActivityBidsInfoBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class BidsInfoActivity extends AppCompatActivity {

    ActivityBidsInfoBinding binding;
    private AuctionItem items;
    private FirebaseFirestore db;
    private CountDownTimer mCountDownTimer;
    private ListenerRegistration listenerRegistration;
    private FirestoreOps firestoreOps;
    private Users users;
    private double userBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBidsInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final String itemId = getIntent().getStringExtra("itemId");
        final String mData = getIntent().getStringExtra("mData");
        db = FirebaseFirestore.getInstance();
        firestoreOps = new FirestoreOps();

        setSupportActionBar(binding.infoToolbar);
        binding.infoToolbar.setNavigationIcon(R.drawable.ic_back);
        binding.infoToolbar.setNavigationOnClickListener(v -> onBackPressed());

        getDetailsFromDB(itemId, mData);

        binding.btnBid.setOnClickListener(v -> updateCurrentBid(itemId));
        if (!Objects.equals(mData, "allBids")) {
            binding.bottomLayout.setVisibility(View.GONE);
        }

    }

    private void updateCurrentBid(String itemId) {
        final String bidAmount = binding.bidAmount.getText().toString().trim();
        double currentBid;
        try {
            currentBid = Double.parseDouble(bidAmount);
        } catch (NumberFormatException e) {
            Toast.makeText(BidsInfoActivity.this, "Please enter a valid bid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreOps.getWalletData(getApplicationContext(), documentSnapshot -> {
            if (documentSnapshot.exists()) {
                users = documentSnapshot.toObject(Users.class);
                assert users != null;
                userBalance = Double.parseDouble(users.getBalance());
            }
        });

        if (currentBid <= items.getCurrentBid()) {
            Toast.makeText(this, "Amount should be more than current bid amount!", Toast.LENGTH_SHORT).show();
        } else if (userBalance < currentBid) {
            Toast.makeText(this, "Not enough amount in your wallet!", Toast.LENGTH_SHORT).show();
        } else {
            Map<String, Object> updateBid = new HashMap<>();
            updateBid.put("currentBid", currentBid);
            updateBid.put("itemId", itemId);
            updateBid.put("buyerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            db.collection("Items").document(itemId).update(updateBid).addOnCompleteListener(task -> {
                binding.bidAmount.setText("");
                if (task.isSuccessful()) {
                    firestoreOps.addWallet(getApplicationContext(), String.valueOf(userBalance - currentBid));
                    Transactions transactions = new Transactions(new Date(), currentBid, "dr", "Bid on item", itemId);
                    firestoreOps.addTrans(getApplicationContext(), transactions);
                    Toast.makeText(BidsInfoActivity.this, "Bid Placed Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BidsInfoActivity.this, "Failed to place bid!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getDetailsFromDB(String itemId, String mData) {
        listenerRegistration = db.collection("Items").document(itemId).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(BidsInfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if (value != null && value.exists()) {
                items = value.toObject(AuctionItem.class);
                if (items != null) {
                    Glide.with(BidsInfoActivity.this).load(items.getItemImage()).into(binding.itemImageD);
                    binding.itemNameD.setText(items.getItemName());
                    binding.itemDescD.setText(items.getItemDesc());
                    String amount = "₹ " + items.getCurrentBid();
                    binding.itemMinD.setText(amount);

                    binding.infoToolbar.setTitle(items.getItemName());
                    switch (mData) {
                        case "myItems":
                        case "allBids":
                            if (items.getExpiryDate().equals("Expired")) {
                                binding.expireTimer.setText(items.getExpiryDate());
                            } else {
                                runTimer(itemId);
                            }
                            break;
                        case "myWins":
                            String win = "Congratulations! You are the winner \uD83C\uDF89";
                            binding.expireTimer.setText(win);
                            binding.expireTimer.setTextColor(getColor(R.color.gold));
                            binding.textView15.setText(getString(R.string.purchased_amount));
                            break;
                    }
                }
            }
        });
    }

    private void runTimer(String itemId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date expiryDate = null;
        try {
            expiryDate = format.parse(items.getExpiryDate());
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        if (expiryDate != null) {
            long millisUntilFinished = expiryDate.getTime() - System.currentTimeMillis();

            mCountDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
                public void onTick(long millisUntilFinished) {
                    // Update UI with the remaining time
                    long days = millisUntilFinished / (1000 * 60 * 60 * 24);
                    long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                    long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                    long seconds = (millisUntilFinished / 1000) % 60;
                    String timeLeft = String.format(Locale.getDefault(), "%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
                    String info = "Expired in: " + timeLeft;
                    binding.expireTimer.setText(info);
                }

                public void onFinish() {
                    // Handle the expiry of the auction item
                    db.collection("Items").document(itemId).update("expiryDate", "Expired");
                    if (items.getBuyerId() != null) {
                        db.collection("Items").document(itemId).update("itemStatus", "Sold");
                    } else {
                        db.collection("Items").document(itemId).update("itemStatus", "Unsold");
                    }
                }
            }.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cancel the countdown timer to avoid memory leaks
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}