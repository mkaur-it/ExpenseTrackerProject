package com.example.expensetrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.expensetrackerapp.databinding.ActivityMainBinding;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnTransactionClick {
    ActivityMainBinding binding;
    private ExpenseAdapter expenseAdapter;
    private long income = 0, expense = 0;
    private String currentType = "Income"; // Initial selection


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expenseAdapter = new ExpenseAdapter(this, this, FirebaseAuth.getInstance().getUid());
        binding.recycler.setAdapter(expenseAdapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);

        binding.addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentType = "Income"; // Update selection
                Log.d("ClickEvent", "Add Income Clicked");
                intent.putExtra("type", currentType);
                startActivity(intent);
            }
        });

        binding.addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentType = "Expense"; // Update selection
                Log.d("ClickEvent", "Add Expense Clicked");
                intent.putExtra("type", currentType);
                startActivity(intent);
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set label visibility mode to always show labels
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    // Handle Home click
                    // You can start the HomeActivity here
                    intent = new Intent(MainActivity.this, MainActivity.class);
                } else if (itemId == R.id.income) {
                    currentType = "Income"; // Update selection
                    Log.d("ClickEvent", "Add Income Clicked");
                    intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                    intent.putExtra("type", currentType);


                } else if (itemId == R.id.expense) {
                    currentType = "Expense"; // Update selection
                    Log.d("ClickEvent", "Add Expense Clicked");
                    intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                    intent.putExtra("type", currentType);

                } else if (itemId == R.id.logout) {
                    // Sign the user out and display a logout toast message
                    // Sign out the user from Firestore (Assuming you have already initialized Firestore)
                    // FirebaseFirestore db = FirebaseFirestore.getInstance();
                    // FirebaseAuth.getInstance().signOut();

                    // Display a logout message
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to the login screen
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the back stack
                    startActivity(intent);
                   // finish(); // Close the current activity
                }

                if (intent != null && itemId != R.id.logout) {
                    startActivity(intent); // Start the selected activity
                }


                return true;
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please");
        progressDialog.setMessage("Wait");
        progressDialog.setCancelable(false);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog.show();
            FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.cancel();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.cancel();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        income = 0;
        expense = 0;
        getData();
    }

    private void getData() {
        FirebaseFirestore
                .getInstance()
                .collection("transactions")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // clear previous data
                        expenseAdapter.clear();
                        List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : dsList) {
                            // fetch data from expense adapter
                            ExpenseModel expenseModel = ds.toObject(ExpenseModel.class);
                            if (expenseModel.getType().equals("Income")) {
                                income += expenseModel.getAmount();
                            } else {
                                expense += expenseModel.getAmount();
                            }
                            expenseAdapter.add(expenseModel);
                        }
                        displayTransactionsGraph();
                    }
                });
    }

    private void displayTransactionsGraph() {
        // Initialize lists to hold pie chart data and colors
        List<PieEntry> pieEntryList = new ArrayList<>();
        List<Integer> colorList = new ArrayList<>();
        int balanceLabelColor = getResources().getColor(R.color.expense_primary);

        // Check if there is non-zero income
        if (income != 0) {
            pieEntryList.add(new PieEntry(income, "Income"));
            colorList.add(getResources().getColor(R.color.expense_green));
        }

        // Check if there is non-zero expense
        if (expense != 0) {
            pieEntryList.add(new PieEntry(expense, "Expense"));
            colorList.add(getResources().getColor(R.color.expense_red));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "Final Balance: " + String.valueOf(income - expense) + " CAD");
        pieDataSet.setColors(colorList);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.white));
        PieData pieData = new PieData(pieDataSet);

        binding.pieChart.setData(pieData);

        // Set description label for the chart
        Description chartDescription = new Description();
        long net = income - expense;
        if(net > 0){
            chartDescription.setText("Balance: $" + String.valueOf(net));
            chartDescription.setTextColor(getResources().getColor(R.color.expense_green));
        }else{
            chartDescription.setText("Balance: -$" + String.valueOf( Math.abs((long) net)));
            chartDescription.setTextColor(getResources().getColor(R.color.expense_red));
        }
         // Set description text color
        chartDescription.setTextSize(14f);
        chartDescription.setPosition(640, 350);
        chartDescription.setTextAlign(Paint.Align.RIGHT);
        binding.pieChart.setDescription(chartDescription);
        binding.pieChart.invalidate();
    }

    @Override
    public void onClick(ExpenseModel expenseModel) {
        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
        intent.putExtra("model", expenseModel);
        startActivity(intent);
    }
}
