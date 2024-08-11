package com.rsdevelopers.auctionhub.Fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rsdevelopers.auctionhub.Models.AuctionItem;
import com.rsdevelopers.auctionhub.R;
import com.rsdevelopers.auctionhub.databinding.FragmentSellBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SellFragment extends Fragment {

    FragmentSellBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private Uri imageUri;

    public SellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellBinding.inflate(inflater, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Adding Details...");
        dialog.setCancelable(false);

        // Set an OnClickListener on the EditText to show the date and time picker
        binding.etExpire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        binding.ivUploadPic.setOnClickListener(v -> imgContent.launch("image/*"));
        binding.tvUploadPic.setOnClickListener(v -> imgContent.launch("image/*"));
        binding.btnSell.setOnClickListener(v -> uploadData());

        return binding.getRoot();
    }

    private void uploadData() {
        final String itemName = String.valueOf(binding.etItemName.getText()).trim();
        final String expDate = String.valueOf(binding.etExpire.getText()).trim();
        String minAmount = String.valueOf(binding.etMinAmount.getText()).trim();
        final String itemDesc = String.valueOf(binding.etDesc.getText()).trim();

        if (itemName.isEmpty()) {
            binding.itemNameLayout.setError("Please enter name");
        } else if (expDate.isEmpty()) {
            binding.expireLayout.setError("Select expiry date and time");
        } else if (minAmount.isEmpty()) {
            binding.minAmountLayout.setError("Please enter minimum bid amount");
        } else if (itemDesc.isEmpty()) {
            binding.descLayout.setError("Please enter item description");
        } else if (imageUri == null) {
            Toast.makeText(getContext(), "Please select item image", Toast.LENGTH_SHORT).show();
        } else {
            dialog.show();
            double currentBid;
            try {
                currentBid = Double.parseDouble(minAmount);
            } catch (NumberFormatException e) {
                // Handle the exception here, e.g. display an error message to the user
                Toast.makeText(getContext(), "Please enter a valid bid amount", Toast.LENGTH_SHORT).show();
                return;
            }
            final String sellerId = auth.getCurrentUser().getUid();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child("items/" + UUID.randomUUID().toString());
            UploadTask uploadTask = ref.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    DocumentReference docRef = db.collection("Items").document();
                    AuctionItem item = new AuctionItem(docRef.getId(), itemName, downloadUri.toString(), itemDesc, expDate, currentBid, sellerId, new Date(), "Bid Running");
                    docRef.set(item).addOnCompleteListener(task1 -> {
                        dialog.dismiss();
                        binding.etItemName.setText("");
                        binding.etExpire.setText("");
                        binding.etMinAmount.setText("");
                        binding.etDesc.setText("");
                        binding.ivUploadPic.setImageResource(R.drawable.ic_upload);
                        if (task1.isSuccessful()) {
                            Toast.makeText(getContext(), "Item added successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Item failed to upload", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void showDateTimePicker() {

        Calendar calendar = Calendar.getInstance();

        // Create a DatePickerDialog to allow the seller to select the expiry date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
            // Update the calendar with the selected date
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Show a TimePickerDialog to allow the seller to select the expiry time
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute) -> {
                // Update the calendar with the selected time
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

//                show it in textbox and set value in a Date object.
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat., Locale.getDefault());
                String expiryDate = dateFormat.format(calendar.getTime());
                binding.etExpire.setText(expiryDate);

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DATE, 1);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();

    }


    ActivityResultLauncher<String> imgContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result != null) {
                binding.ivUploadPic.setImageURI(result);
                imageUri = result;

            }
        }
    });
}
