package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<String> categories;
    private OnCategoryDeleteListener deleteListener;
    
    public interface OnCategoryDeleteListener {
        void onCategoryDelete(String category);
    }
    
    public CategoryAdapter(List<String> categories, OnCategoryDeleteListener deleteListener) {
        this.categories = categories;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);
        
        holder.btnDeleteCategory.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onCategoryDelete(category);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    public void updateCategories(List<String> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }
    
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        Button btnDeleteCategory;
        
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
