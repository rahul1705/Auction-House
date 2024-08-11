package com.rsdevelopers.auctionhub.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rsdevelopers.auctionhub.Activities.BidsInfoActivity;
import com.rsdevelopers.auctionhub.Adapters.BidAdapter;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.databinding.FragmentBidBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BidFragment extends Fragment {

    FragmentBidBinding binding;
    private FirebaseFirestore db;
    private BidAdapter mAdapter;
    private List<AuctionItem> itemList;
    private FirebaseAuth auth;

    public BidFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBidBinding.inflate(inflater, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        itemList = new ArrayList<>();
        // Initialize RecyclerView
//        binding.bidRecycler.setHasFixedSize(true);
        binding.bidRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new BidAdapter(getActivity(), itemList);
        binding.bidRecycler.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(position -> {
            AuctionItem items = itemList.get(position);
            Intent intent = new Intent(getActivity(), BidsInfoActivity.class);
            intent.putExtra("itemId", items.getItemId());
            intent.putExtra("mData", "allBids");
            startActivity(intent);
        });

        try {
            getAuctionData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAuctionData() {
        binding.bidProgress.setVisibility(View.VISIBLE);
        db.collection("Items").whereNotEqualTo("expiryDate", "Expired")
                .orderBy("expiryDate", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.bidProgress.setVisibility(View.GONE);
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.bidRecycler.setVisibility(View.GONE);
                        binding.noItemsBid.setVisibility(View.VISIBLE);
                    } else {
                        itemList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            AuctionItem items = documentSnapshot.toObject(AuctionItem.class);

                            if (!Objects.equals(items.getSellerId(), Objects.requireNonNull(auth.getCurrentUser()).getUid())) {
                                itemList.add(items);
                                binding.noItemsBid.setVisibility(View.GONE);
                                binding.bidRecycler.setVisibility(View.VISIBLE);
                            }
                            if (itemList.isEmpty()) {
                                binding.bidRecycler.setVisibility(View.GONE);
                                binding.noItemsBid.setVisibility(View.VISIBLE);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.d("Error getting documents: ", e.getLocalizedMessage()));
    }

}