package com.example.project;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
    private List<Expense> expenses;
    private Map<String, Integer> categoryColors;
    private OnExpenseClickListener listener;
    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
    }
    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
        setupCategoryColors();
    }
    public void setOnExpenseClickListener(OnExpenseClickListener listener) {
        this.listener = listener;
    }
    private void setupCategoryColors() {
        categoryColors = new HashMap<>();
        categoryColors.put("Potraviny", Color.parseColor("#4CAF50")); 
        categoryColors.put("Bývanie", Color.parseColor("#2196F3")); 
        categoryColors.put("Doprava", Color.parseColor("#FF9800")); 
        categoryColors.put("Zábava", Color.parseColor("#9C27B0")); 
        categoryColors.put("Oblečenie", Color.parseColor("#E91E63")); 
        categoryColors.put("Zdravie", Color.parseColor("#F44336")); 
        categoryColors.put("Jedlo", Color.parseColor("#795548")); 
        categoryColors.put("Iné", Color.parseColor("#607D8B")); 
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
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense);
            }
        });
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
            int categoryColor = categoryColors.getOrDefault(expense.getCategory(), Color.parseColor("#607D8B"));
            tvCategory.setBackgroundColor(categoryColor);
            tvCategory.setTextColor(Color.WHITE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(expense.getDate()));
        }
    }
}
