package com.example.expensetrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.expensetrackerapp.databinding.ActivityAddExpenseBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity {
    ActivityAddExpenseBinding binding;
    private String type;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    // To store the user's UID
    private String userUid;

    // To update transaction entry
    private ExpenseModel expenseModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        type = getIntent().getStringExtra("type");

        // Update transaction entry
        expenseModel = (ExpenseModel) getIntent().getSerializableExtra("model");
        if (expenseModel == null) {
           // type = "Expense"; // Default type for a new transaction
        } else {
            type = expenseModel.getType();
            binding.amount.setText(String.valueOf(expenseModel.getAmount()));
            binding.note.setText(expenseModel.getNote());
            binding.category.setText(expenseModel.getCategory());
        }

        userUid = auth.getUid(); // Get the user's UID

        if (type.equals("Income")) {
            binding.incomeRadio.setChecked(true);
        } else {
            binding.expenseRadio.setChecked(true);
        }

        binding.incomeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "Income";
            }
        });

        binding.expenseRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "Expense";
            }
        });

        // Set up the Toolbar
        Toolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // Remove the title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the navigation icon (back arrow) color to white
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.back_icon);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        // Add a click listener to the back arrow
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event to navigate back to the parent activity (MainActivity)
                onBackPressed();
            }
        });
    }

    // Show Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (expenseModel == null) {
            menuInflater.inflate(R.menu.add_menu, menu);
        } else {
            menuInflater.inflate(R.menu.update_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.saveExpense) {
            if (type != null) {
                createOrUpdateTransaction();
            }

            return true;
        }
        if (id == R.id.deleteExpense) {
            deleteTransaction();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteTransaction() {
        if (expenseModel != null) {
            // Check if the current user's UID matches the UID in the transaction document
            if (userUid.equals(expenseModel.getUid())) {
                db.collection("transactions")
                        .document(expenseModel.getExpenseId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Transaction deleted successfully, show a Toast message.
                                Toast.makeText(getApplicationContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the error if the deletion fails.
                                Toast.makeText(getApplicationContext(), "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Show a message that the user is not authorized to delete this transaction
                Toast.makeText(getApplicationContext(), "You are not authorized to delete this transaction", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createOrUpdateTransaction() {
        String amount = binding.amount.getText().toString();
        String note = binding.note.getText().toString();
        String category = binding.category.getText().toString();

        if (amount.trim().length() == 0) {
            binding.amount.setError("Empty");
            return;
        }

        if (expenseModel == null) {
            // Create a new transaction
            String expenseId = UUID.randomUUID().toString();
            ExpenseModel newExpense = new ExpenseModel(expenseId, note, category, type,
                    Long.parseLong(amount), Calendar.getInstance().getTimeInMillis(), userUid);

            db.collection("transactions")
                    .document(expenseId)
                    .set(newExpense)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Transaction created successfully, show a Toast message.
                            Toast.makeText(getApplicationContext(), "Transaction created", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the error if creating the transaction fails.
                            Toast.makeText(getApplicationContext(), "Failed to create transaction", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Update an existing transaction
            // Check if the current user's UID matches the UID in the transaction document
            if (userUid.equals(expenseModel.getUid())) {
                ExpenseModel updatedExpense = new ExpenseModel(expenseModel.getExpenseId(), note, category, type,
                        Long.parseLong(amount), expenseModel.getTime(), userUid);

                db.collection("transactions")
                        .document(expenseModel.getExpenseId())
                        .set(updatedExpense)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Transaction updated successfully, show a Toast message.
                                Toast.makeText(getApplicationContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the error if updating the transaction fails.
                                Toast.makeText(getApplicationContext(), "Failed to update transaction", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Show a message that the user is not authorized to update this transaction
                Toast.makeText(getApplicationContext(), "You are not authorized to update this transaction", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
