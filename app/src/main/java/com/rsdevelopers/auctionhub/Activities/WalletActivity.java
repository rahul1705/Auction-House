package com.rsdevelopers.auctionhub.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.rsdevelopers.auctionhub.Adapters.TransAdapter;
import com.rsdevelopers.auctionhub.Models.FirestoreOps;
import com.rsdevelopers.auctionhub.Models.Transactions;
import com.rsdevelopers.auctionhub.Models.Users;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.ActivityWalletBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class WalletActivity extends AppCompatActivity implements PaymentResultListener {

    ActivityWalletBinding binding;
    private ArrayList<Transactions> arrayList;
    private TransAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private double amount;
    private Users users;
    private ListenerRegistration listenerRegistration;
    private FirestoreOps firestoreOps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Checkout.preload(WalletActivity.this);
        firestoreOps = new FirestoreOps();

        setSupportActionBar(binding.walletToolbar);
        binding.walletToolbar.setNavigationIcon(R.drawable.ic_back);
        binding.walletToolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        binding.walletRv.setHasFixedSize(true);
        binding.walletRv.setLayoutManager(new LinearLayoutManager(WalletActivity.this));

        arrayList = new ArrayList<>();
        adapter = new TransAdapter(WalletActivity.this, arrayList);
        binding.walletRv.setAdapter(adapter);

        getCardDetails();
        getTransHistory();


        binding.addBalBtn.setOnClickListener(v -> addWalletAmount());
    }

    private void getCardDetails() {
        firestoreOps.getWalletData(WalletActivity.this, documentSnapshot -> {
            if (documentSnapshot.exists()) {
                users = documentSnapshot.toObject(Users.class);
                if (users != null) {
                    String bal = "₹ " + users.getBalance();
                    binding.userName.setText(users.getName());
                    binding.availBal.setText(bal);
                }
            }
        });

    }

    private void addWalletAmount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.amount_add_alert, null);
        TextInputLayout layout_amount = v.findViewById(R.id.amount_add_layout);
        TextInputEditText et_amount_add = v.findViewById(R.id.et_add_amount);

//        Upload code here...

        builder.setPositiveButton(R.string.add, (dialog, which) -> {
            String amount_str = String.valueOf(et_amount_add.getText()).trim();

            try {
                amount = Double.parseDouble(amount_str) * 100;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            if (amount_str.isEmpty()) {
                layout_amount.setError("Enter amount in ₹");
            } else {
                startPayment();
            }
        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.setView(v).setCancelable(false).show();
    }

    public void startPayment() {

        final Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_BlXQcyRtrNKqMG");

        //        checkout.setImage(R.drawable.logo);
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Rahul Mandal");
            options.put("description", "Add Money to Wallet");
            options.put("send_sms_hash", true);
            options.put("allow_rotation", true);
            options.put("currency", "INR");
            options.put("amount", amount);
            options.put("prefill.email", users.getEmail());
            options.put("prefill.contact", users.getMobile());
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(WalletActivity.this, options);

        } catch (Exception e) {
            Toast.makeText(WalletActivity.this, "Error in starting Razorpay Checkout " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getTransHistory() {
        listenerRegistration = db.collection("Users").document(auth.getCurrentUser().getUid()).collection("Transactions")
                .orderBy("transDate", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore error: ", error.getMessage());
                        return;
                    }
                    assert value != null;
                    if (value.isEmpty()) {
                        binding.walletRv.setVisibility(View.GONE);
                        binding.noTrans.setVisibility(View.VISIBLE);
                    } else {
                        binding.walletRv.setVisibility(View.VISIBLE);
                        binding.noTrans.setVisibility(View.GONE);
                        arrayList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            Transactions transactions = document.toObject(Transactions.class);
                            if (transactions != null) {
                                try {
                                    transactions.setTransId(document.getId());
                                    arrayList.add(transactions);
                                } catch (Exception e) {
                                    Toast.makeText(WalletActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(WalletActivity.this, "No Data found!", Toast.LENGTH_SHORT).show();
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void onPaymentSuccess(String s) {
        String am = String.valueOf((amount / 100) + Float.parseFloat(users.getBalance()));
        firestoreOps.addWallet(getApplicationContext(), am);
        Transactions transactions = new Transactions(new Date(), amount / 100, "cr", "Added to Wallet", "");
        firestoreOps.addTrans(getApplicationContext(), transactions);
        Toast.makeText(this, "Success payment", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Fail to make the payment!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        firestoreOps.unregisterListener();
    }
}