package com.rsdevelopers.auctionhub.Activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rsdevelopers.auctionhub.Models.FirestoreOps;
import com.rsdevelopers.auctionhub.Models.Users;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.ActivityUpdateProfileBinding;

import java.util.HashMap;

public class UpdateProfileActivity extends AppCompatActivity {

    ActivityUpdateProfileBinding binding;
    private Users users;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private FirestoreOps firestoreOps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.upPfToolbar);
        binding.upPfToolbar.setNavigationIcon(R.drawable.ic_back);
        binding.upPfToolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firestoreOps = new FirestoreOps();
        dialog = new ProgressDialog(UpdateProfileActivity.this);
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);


        firestoreOps.getUserData(getApplicationContext(), documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User data found in Firestore
                users = documentSnapshot.toObject(Users.class);
                if (users != null) {
                    binding.etUpName.setText(users.getName());
                    binding.etUpEmail.setText(users.getEmail());
                    binding.etUpMobile.setText(users.getMobile());
                    Glide.with(UpdateProfileActivity.this).load(users.getUserImage()).placeholder(R.drawable.profile).into(binding.upProfileImage);
                }
            } else {
                // User data not found in Firestore
                Toast.makeText(UpdateProfileActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.upBtn.setOnClickListener(v -> updateProfile());

        binding.upProfileImage.setOnClickListener(v -> imgContent.launch("image/*"));

    }

    private void updateProfile() {

        final String name = String.valueOf(binding.etUpName.getText()).trim();
        final String email = String.valueOf(binding.etUpEmail.getText()).trim();
        final String mobile = String.valueOf(binding.etUpMobile.getText()).trim();
        if (name.isEmpty()) {
            binding.upNameLayout.setError("Please enter name");
        } else if (email.isEmpty()) {
            binding.upEmailLayout.setError("Please enter email");
        } else if (mobile.isEmpty()) {
            binding.upMobLayout.setError("Please enter number");
        } else {
            dialog.show();
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("email", email);
            map.put("mobile", mobile);
            db.collection("Users").document(auth.getCurrentUser().getUid()).update(map)
                    .addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            onBackPressed();
                            Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, "Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }


    //    Upload Image and update
    ActivityResultLauncher<String> imgContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result != null) {
                dialog.show();
                StorageReference ref = FirebaseStorage.getInstance().getReference().child("users/" + auth.getCurrentUser().getUid());
                UploadTask uploadTask = ref.putFile(result);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        db.collection("Users").document(auth.getCurrentUser().getUid()).update("userImage", downloadUri.toString())
                                .addOnCompleteListener(task1 -> {
                                    dialog.dismiss();
                                    if (task1.isSuccessful()) {
                                        onBackPressed();
                                        Toast.makeText(UpdateProfileActivity.this, "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UpdateProfileActivity.this, "Error: " + task1.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firestoreOps.unregisterListener();
    }

}