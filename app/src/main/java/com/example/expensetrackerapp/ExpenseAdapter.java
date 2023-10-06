package com.example.expensetrackerapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.type.Date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.MyViewHolder> {
    private Context context;
    private OnTransactionClick onTransactionClick;
    private List<ExpenseModel> expenseModelList;
    private String currentUserUid; // Store the current user's UID

    public ExpenseAdapter(Context context, OnTransactionClick onTransactionClick, String currentUserUid) {
        this.context = context;
        expenseModelList = new ArrayList<>();
        this.onTransactionClick = onTransactionClick;
        this.currentUserUid = currentUserUid; // Initialize the current user's UID
    }

    public void add(ExpenseModel expenseModel) {
        expenseModelList.add(expenseModel);
        sortExpenseListByTime();
        notifyDataSetChanged();
    }

    private void sortExpenseListByTime() {
        Collections.sort(expenseModelList, new Comparator<ExpenseModel>() {
            @Override
            public int compare(ExpenseModel o1, ExpenseModel o2) {
                // Assuming you have a getTime() method in ExpenseModel
                long time1 = o1.getTime();
                long time2 = o2.getTime();
                // Sorting in descending order (most recent first)
                return Long.compare(time2, time1);
            }
        });
    }

    public void clear() {
        expenseModelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ExpenseModel expenseModel = expenseModelList.get(position);

        // Check if the current user's UID matches the UID in the ExpenseModel
        if (currentUserUid.equals(expenseModel.getUid())) {
            holder.note.setText(expenseModel.getNote());
            holder.category.setText(expenseModel.getCategory());
            holder.amount.setText(String.valueOf(expenseModel.getAmount()));

            // Append "CAD" to the amount and set the text
            String amountText = String.valueOf(expenseModel.getAmount()) + " CAD";


            // Set text color based on transaction type
            if (expenseModel.getType().equals("Income")) {
                holder.amount.setTextColor(context.getResources().getColor(R.color.expense_green));
                amountText = "+" + amountText;
            } else {
                holder.amount.setTextColor(context.getResources().getColor(R.color.expense_red));
                amountText = "-" + amountText;
            }
            holder.amount.setText(amountText);
            // date to be added here

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTransactionClick.onClick(expenseModel);
                }
            });
        } else {
            // Hide the view or display a message indicating that the user is not authorized
            holder.note.setText("Not Authorized");
            holder.category.setVisibility(View.GONE);
            holder.amount.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null); // Disable click for unauthorized items
        }
    }


    @Override
    public int getItemCount() {
        return expenseModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView note, category, amount, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            note = itemView.findViewById(R.id.note);
            category = itemView.findViewById(R.id.category);
            amount = itemView.findViewById(R.id.amount);
        }
    }
}
