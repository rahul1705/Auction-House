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
import com.rsdevelopers.auctionhub.databinding.ActivityLoginBinding;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^" + "(?=.*[@#$%^&+=])" +     // at least 1 special character
            "(?=\\S+$)" +            // no white spaces
            ".{4,}" +                // at least 4 characters
            "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Logging in...");
        dialog.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> {

            final String email = binding.etEmailLogin.getText().toString().trim();
            final String pass = binding.etPassLogin.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                binding.etEmailLogin.setError("Required");
                binding.etEmailLogin.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmailLogin.setError("Invalid email");
                binding.etEmailLogin.requestFocus();
            } else if (TextUtils.isEmpty(pass)) {
                binding.etPassLogin.setError("Required");
                binding.etPassLogin.requestFocus();
            } else if (pass.length() < 8 || !PASSWORD_PATTERN.matcher(pass).matches()) {
                binding.etPassLogin.setError("Must be of minimum 8 Characters, 1 special character and no white spaces");
                binding.etPassLogin.requestFocus();
            } else {
                binding.etEmailLogin.setError(null);
                binding.etPassLogin.setError(null);
                dialog.show();

                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
//                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.registerRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
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