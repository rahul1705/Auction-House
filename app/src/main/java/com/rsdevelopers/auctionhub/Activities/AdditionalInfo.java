package com.rsdevelopers.auctionhub.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.ActivityAdditionalInfoBinding;

public class AdditionalInfo extends AppCompatActivity {

    ActivityAdditionalInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdditionalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.additionalToolbar.setNavigationIcon(R.drawable.ic_back);
        binding.additionalToolbar.setNavigationOnClickListener(v -> onBackPressed());

        final String data = getIntent().getStringExtra("data");

        switch (data) {
            case "about":
                binding.additionalToolbar.setTitle("About Us");
                binding.webview.loadUrl("file:///android_res/raw/about_us.html");
                break;
            case "policy":
                binding.additionalToolbar.setTitle("Privacy Policy");
                binding.webview.loadUrl("file:///android_res/raw/privacy_policy.html");
                break;
            case "contact":
                binding.additionalToolbar.setTitle("Contact Us");
                binding.webview.setVisibility(View.GONE);
                binding.contactLayout.setVisibility(View.VISIBLE);
                break;
        }

    }
}