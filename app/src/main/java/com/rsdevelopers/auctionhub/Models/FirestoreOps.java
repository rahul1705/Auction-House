package com.rsdevelopers.auctionhub.Models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class FirestoreOps {

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    public FirestoreOps() {
        db = FirebaseFirestore.getInstance();
    }

    public void unregisterListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    // Add user's balance data to Firestore
    public void addWallet(Context context, String wallet_amount) {
        db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update("balance", wallet_amount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User's balance data added to Firestore
//                        Toast.makeText(context, "Balance Added to database", Toast.LENGTH_SHORT).show();
                        Log.e("Add Wallet", "Amount added to wallet");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(context, "Add Wallet error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Static method to retrieve data from Firebase
    public void getWalletData(Context context, final FirebaseCallback callback) {
        ListenerRegistration listenerRegistration = db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null && value.exists()) {
                        callback.onCallback(value);
                    }
                });
    }

    //    Retrieve users data
    public void getUserData(Context context, final FirebaseCallback callback) {
        db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        callback.onCallback(document);
                    } else {
                        Toast.makeText(context, "Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //    add transactions details to db
    public void addTrans(Context context, Transactions data) {
        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Transactions")
                .add(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
//                        Toast.makeText(context, "Amount added to wallet!", Toast.LENGTH_SHORT).show();
                        Log.e("Add Trans", "Amount added to wallet");
                    } else {
                        Toast.makeText(context, "Failed to add balance to wallet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Define a callback interface to handle the data
    public interface FirebaseCallback {
        void onCallback(DocumentSnapshot documentSnapshot);
    }
}