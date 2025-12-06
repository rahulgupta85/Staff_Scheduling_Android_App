package com.example.edutech.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutech.R;
import com.example.edutech.User; // <-- ADD THIS IMPORT LINE
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<User> staffList;

    public StaffAdapter(List<User> staffList) {
        this.staffList = staffList;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User user = staffList.get(position);
        String fullName = user.getFirstName() + " " + user.getLastName();
        holder.staffName.setText(fullName);
        holder.staffEmail.setText(user.getEmail());
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView staffName;
        TextView staffEmail;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            staffName = itemView.findViewById(R.id.textViewStaffName);
            staffEmail = itemView.findViewById(R.id.textViewStaffEmail);
        }
    }
}