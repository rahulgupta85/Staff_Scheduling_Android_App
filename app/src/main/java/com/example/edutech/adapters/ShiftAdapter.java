package com.example.edutech.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutech.R;
import com.example.edutech.Shift;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ViewHolder> {

    private List<Shift> shiftList;
    private OnShiftListener listener;
    private boolean isAdmin;

    public interface OnShiftListener {
        void onDeleteClick(int position);
    }

    public ShiftAdapter(List<Shift> shiftList, boolean isAdmin, OnShiftListener listener) {
        this.shiftList = shiftList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shift, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shift shift = shiftList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        holder.shiftDate.setText(dateFormat.format(shift.getStartTime().toDate()));
        holder.userName.setText(shift.getUserName());
        holder.shiftDetails.setText(shift.getShiftDetails());
        String startTime = timeFormat.format(shift.getStartTime().toDate());
        String endTime = timeFormat.format(shift.getEndTime().toDate());
        holder.shiftTime.setText(startTime + " - " + endTime);

        if (isAdmin) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return shiftList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView shiftDate, userName, shiftTime, shiftDetails;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView, OnShiftListener listener) {
            super(itemView);
            shiftDate = itemView.findViewById(R.id.textViewShiftDate);
            userName = itemView.findViewById(R.id.textViewUserName);
            shiftTime = itemView.findViewById(R.id.textViewShiftTime);
            shiftDetails = itemView.findViewById(R.id.textViewShiftDetails);
            // --- THIS LINE WAS MISSING ---
            deleteButton = itemView.findViewById(R.id.btn_delete_shift);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}