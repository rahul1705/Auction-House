package com.rsdevelopers.auctionhub.Fragments;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rsdevelopers.auctionhub.Activities.BidsInfoActivity;
import com.rsdevelopers.auctionhub.Adapters.MyItemsAdapter;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.databinding.FragmentMyItemsBinding;

import java.util.ArrayList;
import java.util.List;

public class MyItemsFragment extends Fragment {

    FragmentMyItemsBinding binding;
    private FirebaseFirestore db;
    private MyItemsAdapter mAdapter;
    private List<AuctionItem> itemList;
    private FirebaseAuth auth;

    public MyItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyItemsBinding.inflate(inflater, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        itemList = new ArrayList<>();
        // Initialize RecyclerView
//        binding.myItemsRv.setHasFixedSize(true);
        binding.myItemsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MyItemsAdapter(getActivity(), itemList);
        binding.myItemsRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(position -> {
            AuctionItem items = itemList.get(position);
            Intent intent = new Intent(getActivity(), BidsInfoActivity.class);
            intent.putExtra("itemId", items.getItemId());
            intent.putExtra("mData", "myItems");
            startActivity(intent);
        });

        try {
            getMyItems();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return binding.getRoot();
    }

    private void getMyItems() {
        binding.bidProgress.setVisibility(View.VISIBLE);
        db.collection("Items").whereEqualTo("sellerId", auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.bidProgress.setVisibility(View.GONE);
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.noItemsBid.setVisibility(View.VISIBLE);
                        binding.myItemsRv.setVisibility(View.GONE);
                    } else {
                        itemList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            AuctionItem items = documentSnapshot.toObject(AuctionItem.class);
                            binding.noItemsBid.setVisibility(View.GONE);
                            binding.myItemsRv.setVisibility(View.VISIBLE);
                            items.setItemId(documentSnapshot.getId());
                            itemList.add(items);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.d("Error getting documents: ", e.getLocalizedMessage()));
    }
}