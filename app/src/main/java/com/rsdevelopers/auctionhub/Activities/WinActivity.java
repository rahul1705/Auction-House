package com.rsdevelopers.auctionhub.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rsdevelopers.auctionhub.Adapters.MyItemsAdapter;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.ActivityWinHistoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WinActivity extends AppCompatActivity {

    ActivityWinHistoryBinding binding;
    private FirebaseFirestore db;
    private MyItemsAdapter mAdapter;
    private List<AuctionItem> itemList;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWinHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.winningsToolbar);
        binding.winningsToolbar.setNavigationIcon(R.drawable.ic_back);
        binding.winningsToolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        itemList = new ArrayList<>();
        // Initialize RecyclerView
        binding.winningsRv.setHasFixedSize(true);
        binding.winningsRv.setLayoutManager(new LinearLayoutManager(WinActivity.this));
        mAdapter = new MyItemsAdapter(WinActivity.this, itemList);
        binding.winningsRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(position -> {
            AuctionItem items = itemList.get(position);
            Intent intent = new Intent(WinActivity.this, BidsInfoActivity.class);
            intent.putExtra("itemId", items.getItemId());
            intent.putExtra("mData", "myWins");
            startActivity(intent);
        });

        try {
            getMyWins();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMyWins() {
        binding.bidProgress.setVisibility(View.VISIBLE);
        db.collection("Items").whereEqualTo("expiryDate", "Expired").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.bidProgress.setVisibility(View.GONE);
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.noItemsBid.setVisibility(View.VISIBLE);
                        binding.winningsRv.setVisibility(View.GONE);
                    } else {
                        itemList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            AuctionItem items = documentSnapshot.toObject(AuctionItem.class);
                            binding.noItemsBid.setVisibility(View.GONE);
                            binding.winningsRv.setVisibility(View.VISIBLE);
                            items.setItemId(documentSnapshot.getId());
                            if (Objects.equals(items.getBuyerId(), auth.getCurrentUser().getUid())) {
                                itemList.add(items);
                                return;
                            }
                            if (itemList.isEmpty()) {
                                binding.noItemsBid.setVisibility(View.VISIBLE);
                                binding.winningsRv.setVisibility(View.GONE);
                                return;
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.d("Error getting documents: ", e.getLocalizedMessage()));
    }
}