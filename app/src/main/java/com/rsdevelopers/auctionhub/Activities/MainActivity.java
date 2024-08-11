package com.rsdevelopers.auctionhub.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rsdevelopers.auctionhub.Fragments.BidFragment;
import com.rsdevelopers.auctionhub.Fragments.MyItemsFragment;
import com.rsdevelopers.auctionhub.Fragments.SellFragment;
import com.rsdevelopers.auctionhub.Fragments.SettingsFragment;
import com.rsdevelopers.auctionhub.Models.FirestoreOps;
import com.rsdevelopers.auctionhub.Models.Users;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private Users users;
    private FirestoreOps firestoreOps;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new BidFragment());

        firestoreOps = new FirestoreOps();
        try {
            loadWalletData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bid:
                    replaceFragment(new BidFragment());
                    break;
                case R.id.sell:
                    replaceFragment(new SellFragment());
                    break;
                case R.id.myItems:
                    replaceFragment(new MyItemsFragment());
                    break;
                case R.id.settings:
                    replaceFragment(new SettingsFragment());
                    break;
            }
            return true;
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }

    private void loadWalletData() {
        firestoreOps.getWalletData(getApplicationContext(), documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User data found in Firestore
                users = documentSnapshot.toObject(Users.class);
                assert users != null;
                String userBalance = "\uD83D\uDC5B " + users.getBalance();
                binding.topAppBar.getMenu().findItem(R.id.bal_amount).setTitle(userBalance);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firestoreOps.unregisterListener();
    }
}