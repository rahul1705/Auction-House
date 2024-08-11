package com.rsdevelopers.auctionhub.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rsdevelopers.auctionhub.Models.Users;
import com.rsdevelopers.auctionhub.databinding.ActivityRegisterBinding;

import java.util.Date;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private ProgressDialog dialog;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^" + "(?=.*[@#$%^&+=])" +     // at least 1 special character
            "(?=\\S+$)" +            // no white spaces
            ".{4,}" +                // at least 4 characters
            "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dialog = new ProgressDialog(RegisterActivity.this);
        dialog.setMessage("Creating Account...");
        dialog.setCancelable(false);

        binding.btnSp.setOnClickListener(v -> {

            final String name = binding.etName.getText().toString().trim();
            final String mobile = binding.etMobile.getText().toString().trim();
            final String email = binding.etEmailSp.getText().toString().trim();
            final String pass = binding.etPassSp.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                binding.etName.setError("Required");
                binding.etName.requestFocus();
            } else if (TextUtils.isEmpty(mobile)) {
                binding.etMobile.setError("Required");
                binding.etMobile.requestFocus();
            } else if (mobile.length() != 10) {
                binding.etMobile.setError("Invalid Number");
                binding.etMobile.requestFocus();
            } else if (TextUtils.isEmpty(email)) {
                binding.etEmailSp.setError("Required");
                binding.etEmailSp.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmailSp.setError("Invalid email");
                binding.etEmailSp.requestFocus();
            } else if (TextUtils.isEmpty(pass)) {
                binding.etPassSp.setError("Required");
                binding.etPassSp.requestFocus();
            } else if (pass.length() < 8 || !PASSWORD_PATTERN.matcher(pass).matches()) {
                binding.etPassSp.setError("Must be of minimum 8 Characters, 1 special character and no white spaces");
                binding.etPassSp.requestFocus();
            } else {
                binding.etName.setError(null);
                binding.etMobile.setError(null);
                binding.etEmailSp.setError(null);
                binding.etPassSp.setError(null);

                dialog.show();
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userId = mAuth.getCurrentUser().getUid();
                        final Users users = new Users(name, mobile, email, pass, new Date());
                        db.collection("Users").document(userId).set(users).addOnCompleteListener(task1 -> {
                            dialog.dismiss();
                            if (task1.isSuccessful()) {
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, task1.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // If sign in fails, display a message to the user.
                        dialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        binding.loginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}