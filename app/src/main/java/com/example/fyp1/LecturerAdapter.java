package com.example.fyp1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LecturerAdapter extends RecyclerView.Adapter<LecturerAdapter.LecturerViewHolder> {

    private List<Lecturer> lecturers;
    private OnLecturerClickListener listener;

    public interface OnLecturerClickListener {
        void onLecturerClick(Lecturer lecturer);
    }

    public LecturerAdapter(List<Lecturer> lecturers, OnLecturerClickListener listener) {
        this.lecturers = lecturers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LecturerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lecturer, parent, false);
        return new LecturerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LecturerViewHolder holder, int position) {
        Lecturer lecturer = lecturers.get(position);
        holder.tvName.setText(lecturer.getName());
        holder.tvPhone.setText(lecturer.getPhone());
        holder.ivProfile.setImageResource(lecturer.getProfileImageResId());

        holder.itemView.setOnClickListener(v -> listener.onLecturerClick(lecturer));
    }

    @Override
    public int getItemCount() {
        return lecturers.size();
    }

    public void updateList(List<Lecturer> newList) {
        this.lecturers = newList;
        notifyDataSetChanged();
    }

    static class LecturerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvPhone;

        public LecturerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivLecturerProfile);
            tvName = itemView.findViewById(R.id.tvLecturerName);
            tvPhone = itemView.findViewById(R.id.tvLecturerPhone);
        }
    }
}