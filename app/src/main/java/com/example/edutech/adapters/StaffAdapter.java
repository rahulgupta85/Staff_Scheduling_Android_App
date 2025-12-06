package com.example.edutech.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutech.R;
import com.example.edutech.User;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
    private List<User> staffList;
    public StaffAdapter(List<User> staffList) { this.staffList = staffList; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_staff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = staffList.get(position);
        holder.staffName.setText(user.getFirstName() + " " + user.getLastName());
        holder.staffEmail.setText(user.getEmail());
    }

    @Override
    public int getItemCount() { return staffList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView staffName, staffEmail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            staffName = itemView.findViewById(R.id.textViewStaffName);
            staffEmail = itemView.findViewById(R.id.textViewStaffEmail);
        }
    }
}