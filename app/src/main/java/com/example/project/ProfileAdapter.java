package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    
    private List<String> profiles;
    private String currentProfile;
    private OnProfileActionListener listener;
    
    public interface OnProfileActionListener {
        void onProfileSelect(String profile);
        void onProfileDelete(String profile);
    }
    
    public ProfileAdapter(List<String> profiles, String currentProfile, OnProfileActionListener listener) {
        this.profiles = profiles;
        this.currentProfile = currentProfile;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        String profile = profiles.get(position);
        holder.bind(profile, profile.equals(currentProfile));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileSelect(profile);
            }
        });
        
        holder.btnDeleteProfile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileDelete(profile);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return profiles.size();
    }
    
    public void updateProfiles(List<String> newProfiles, String newCurrentProfile) {
        this.profiles = newProfiles;
        this.currentProfile = newCurrentProfile;
        notifyDataSetChanged();
    }
    
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvProfileName;
        Button btnDeleteProfile;
        View itemView;
        
        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvProfileName = itemView.findViewById(R.id.tvProfileName);
            btnDeleteProfile = itemView.findViewById(R.id.btnDeleteProfile);
        }
        
        void bind(String profileName, boolean isSelected) {
            tvProfileName.setText(profileName);
            
            // Highlight current profile
            if (isSelected) {
                itemView.setBackgroundColor(0xFFE3F2FD); // Light blue background
                tvProfileName.setTextColor(0xFF1976D2); // Blue text
            } else {
                itemView.setBackgroundColor(0xFFFFFFFF); // White background
                tvProfileName.setTextColor(0xFF000000); // Black text
            }
        }
    }
}
