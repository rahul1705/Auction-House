package com.rsdevelopers.auctionhub.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.rsdevelopers.auctionhub.Activities.AdditionalInfo;
import com.rsdevelopers.auctionhub.Activities.LoginActivity;
import com.rsdevelopers.auctionhub.Activities.UpdateProfileActivity;
import com.rsdevelopers.auctionhub.Activities.WalletActivity;
import com.rsdevelopers.auctionhub.Activities.WinActivity;
import com.rsdevelopers.auctionhub.Models.FirestoreOps;
import com.rsdevelopers.auctionhub.Models.Users;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    FragmentSettingsBinding binding;
    private Users users;
    private FirebaseAuth mAuth;
    private FirestoreOps firestoreOps;

    public SettingsFragment() {
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
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        firestoreOps = new FirestoreOps();

        loadProfileData();

        binding.aboutBtn.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AdditionalInfo.class);
            i.putExtra("data", "about");
            startActivity(i);
        });
        binding.policyBtn.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AdditionalInfo.class);
            i.putExtra("data", "policy");
            startActivity(i);
        });

        binding.contactBtn.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AdditionalInfo.class);
            i.putExtra("data", "contact");
            startActivity(i);
        });

        binding.shareBtn.setOnClickListener(v -> {
            // Set up the sharing intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String subject = "Check out this auction app!";
            String message = "I've been using this auction app and it's great. You should check it out!";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            // Launch the sharing dialog
            startActivity(Intent.createChooser(shareIntent, "Share using"));
        });

        binding.historyBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), WinActivity.class)));
        binding.updatePTv.setOnClickListener(v -> startActivity(new Intent(getContext(), UpdateProfileActivity.class)));

        binding.walletBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), WalletActivity.class)));
        binding.logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return binding.getRoot();
    }

    private void loadProfileData() {
        firestoreOps.getUserData(getContext(), documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User data found in Firestore
                users = documentSnapshot.toObject(Users.class);
                if (users != null) {
                    binding.dUserName.setText(users.getName());
                    binding.dUserEmail.setText(users.getEmail());
                    Glide.with(requireContext()).load(users.getUserImage()).placeholder(R.drawable.profile).into(binding.profileImage);
                }
            } else {
                // User data not found in Firestore
                binding.dUserName.setText(R.string.error_name);
                binding.dUserEmail.setText(R.string.error_email);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firestoreOps.unregisterListener();
    }

}