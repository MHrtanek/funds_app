package com.example.project;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
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

            // Farba sumy podľa výšky výdavku
            double amount = expense.getAmount();
            if (amount >= 100) {
                tvAmount.setTextColor(Color.parseColor("#F44336"));
            } else if (amount >= 30) {
                tvAmount.setTextColor(Color.parseColor("#FF9800"));
            } else {
                tvAmount.setTextColor(Color.parseColor("#4CAF50"));
            }

            tvDescription.setText(expense.getDescription());
            tvCategory.setText(expense.getCategory());

            float density = itemView.getResources().getDisplayMetrics().density;

            // Zaokrúhlené rohy karty + tieň
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(Color.WHITE);
            cardBg.setStroke(1, Color.parseColor("#DDDDDD"));
            cardBg.setCornerRadius(16 * density);
            itemView.setBackground(cardBg);
            itemView.setElevation(8f);
            itemView.setTranslationZ(4f);

            // Pill-tvar badge pre kategóriu
            int categoryColor = categoryColors.getOrDefault(expense.getCategory(), Color.parseColor("#607D8B"));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(categoryColor);
            badgeBg.setCornerRadius(50 * density);
            tvCategory.setBackground(badgeBg);
            tvCategory.setTextColor(Color.WHITE);

            // Dátum: "Dnes" / "Včera" / formát
            Calendar expenseCal = Calendar.getInstance();
            expenseCal.setTime(expense.getDate());
            expenseCal.set(Calendar.HOUR_OF_DAY, 0);
            expenseCal.set(Calendar.MINUTE, 0);
            expenseCal.set(Calendar.SECOND, 0);
            expenseCal.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar yesterday = Calendar.getInstance();
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);
            yesterday.add(Calendar.DAY_OF_MONTH, -1);

            String dateText;
            if (expenseCal.equals(today)) {
                dateText = "Dnes";
            } else if (expenseCal.equals(yesterday)) {
                dateText = "Včera";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateText = sdf.format(expense.getDate());
            }
            tvDate.setText(dateText);
        }
    }
}
