package com.naufal.younifirst.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.naufal.younifirst.R;
import java.util.List;

public class YearAdapter extends RecyclerView.Adapter<YearAdapter.ViewHolder> {
    private List<Integer> years;
    private int selectedPosition = -1;
    private OnYearClickListener listener;

    public interface OnYearClickListener {
        void onYearClick(int year, int position);
    }

    public YearAdapter(List<Integer> years, OnYearClickListener listener) {
        this.years = years;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_year, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int year = years.get(position);
        holder.yearText.setText(String.valueOf(year));

        // Set selected state - ini yang mengatur warna background
        boolean isSelected = (position == selectedPosition);
        holder.itemView.setSelected(isSelected);
        holder.yearText.setSelected(isSelected);

        holder.itemView.setOnClickListener(v -> {
            // Update selected position
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Notify item changes untuk update warna
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);

            // Panggil listener
            if (listener != null) {
                listener.onYearClick(year, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return years.size();
    }

    public int getSelectedYear() {
        return (selectedPosition != -1) ? years.get(selectedPosition) : -1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView yearText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            yearText = itemView.findViewById(R.id.yearText);
        }
    }
}