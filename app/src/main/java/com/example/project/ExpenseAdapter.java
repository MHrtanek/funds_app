package com.example.project;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses;
    private Map<String, Integer> categoryColors;
    private Map<String, Integer> categoryIcons;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
        setupCategoryColors();
        setupCategoryIcons();
    }

    public void setOnExpenseClickListener(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    private void setupCategoryColors() {
        categoryColors = new HashMap<>();
        categoryColors.put("Potraviny", Color.parseColor("#4CAF50")); // Zelená
        categoryColors.put("Bývanie", Color.parseColor("#2196F3")); // Modrá
        categoryColors.put("Doprava", Color.parseColor("#FF9800")); // Oranžová
        categoryColors.put("Zábava", Color.parseColor("#9C27B0")); // Fialová
        categoryColors.put("Oblečenie", Color.parseColor("#E91E63")); // Ružová
        categoryColors.put("Zdravie", Color.parseColor("#F44336")); // Červená
        categoryColors.put("Jedlo", Color.parseColor("#795548")); // Hnedá
        categoryColors.put("Iné", Color.parseColor("#607D8B")); // Modrošedá
    }

    private void setupCategoryIcons() {
        categoryIcons = new HashMap<>();
        categoryIcons.put("Potraviny", R.drawable.ic_food);
        categoryIcons.put("Bývanie", R.drawable.ic_home);
        categoryIcons.put("Doprava", R.drawable.ic_transport);
        categoryIcons.put("Zábava", R.drawable.ic_entertainment);
        categoryIcons.put("Oblečenie", R.drawable.ic_clothing);
        categoryIcons.put("Zdravie", R.drawable.ic_health);
        categoryIcons.put("Jedlo", R.drawable.ic_food);
        categoryIcons.put("Iné", R.drawable.ic_other);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense);
            }
        });

        // Set long click listener for deletion
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onExpenseLongClick(expense);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAmount, tvDescription, tvCategory, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(Expense expense) {
            tvAmount.setText(String.format("%.2f €", expense.getAmount()));
            tvDescription.setText(expense.getDescription());
            tvCategory.setText(expense.getCategory());

            // Nastavíme farbu kategórie
            int categoryColor = categoryColors.getOrDefault(expense.getCategory(), Color.parseColor("#607D8B"));
            tvCategory.setBackgroundColor(categoryColor);
            tvCategory.setTextColor(Color.WHITE);

            // Nastavíme ikonu kategórie
            int iconRes = categoryIcons.getOrDefault(expense.getCategory(), R.drawable.ic_other);
            Drawable icon = ContextCompat.getDrawable(tvCategory.getContext(), iconRes);
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                tvCategory.setCompoundDrawables(icon, null, null, null);
                tvCategory.setCompoundDrawablePadding(8);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(expense.getDate()));
        }
    }
}
