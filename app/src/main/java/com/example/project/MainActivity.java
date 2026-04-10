package com.example.project;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class MainActivity extends AppCompatActivity {
    private RecyclerView expensesRecyclerView;
    private Button btnAdd, btnViewMore, btnDaily, btnYearly, btnMonthly, btnSetBudget;
    private LineChart expenseChart;
    private PieChart pieChart;
    private TextView monthlyTotalTextView, dailyAverageTextView, lastMonthTotalTextView, monthlyChangeTextView;
    private TextView tvEmptyState, tvBudgetStatus, tvChartEmpty;
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> recentExpenses = new ArrayList<>();
    private ExpenseAdapter adapter;
    private String currentFilter = "Deň";
    private DataManager dataManager;
    private int currentStatusBarColor = Color.parseColor("#4CAF50"); // Predvolená zelená farba
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        EdgeToEdge.enable(this);
        updateStatusBarColor();
        setContentView(R.layout.activity_main);
        dataManager = new DataManager(this);
        allExpenses = dataManager.loadExpenses();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right + 16, systemBars.bottom);
            return insets;
        });
        TextView dateTextView = findViewById(R.id.dateTextView);
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnViewMore = findViewById(R.id.btnViewMore);
        btnDaily = findViewById(R.id.btnDaily);
        btnMonthly = findViewById(R.id.btnMonthly);
        btnYearly = findViewById(R.id.btnYearly);
        expenseChart = findViewById(R.id.expenseChart);
        monthlyTotalTextView = findViewById(R.id.monthlyTotalTextView);
        dailyAverageTextView = findViewById(R.id.dailyAverageTextView);
        lastMonthTotalTextView = findViewById(R.id.lastMonthTotalTextView);
        monthlyChangeTextView = findViewById(R.id.monthlyChangeTextView);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
        tvChartEmpty = findViewById(R.id.tvChartEmpty);
        pieChart = findViewById(R.id.pieChart);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnSetBudget.setOnClickListener(v -> showBudgetDialog());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateTextView.setText(currentDate);
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupChart();
        setupFilterButtons();
        updateMonthlyTotals();
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, 1);
        });
        btnViewMore.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllExpensesActivity.class);
            startActivity(intent);
        });
        if (allExpenses.isEmpty()) {
            loadExpensesFromStorage();
        }
        updateRecentExpenses();
        setupSwipeToDelete();
        checkViewMoreButton();
        updateChart();
        updatePieChart();
        updateMonthlyTotals();
        checkBudgetWarning();
    }
    @Override
    protected void onResume() {
        super.onResume();
        allExpenses = dataManager.loadExpenses();
        updateRecentExpenses();
        checkViewMoreButton();
        updateChart();
        updatePieChart();
        updateMonthlyTotals();
        checkBudgetWarning();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadExpensesFromStorage();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            Expense editedExpense = (Expense) data.getSerializableExtra("EDITED_EXPENSE");
            if (editedExpense != null) {
                for (int i = 0; i < allExpenses.size(); i++) {
                    if (allExpenses.get(i).getId().equals(editedExpense.getId())) {
                        allExpenses.set(i, editedExpense);
                        break;
                    }
                }
                dataManager.updateExpense(editedExpense);
                for (int i = 0; i < recentExpenses.size(); i++) {
                    if (recentExpenses.get(i).getId().equals(editedExpense.getId())) {
                        recentExpenses.set(i, editedExpense);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
                updateChart();
                updatePieChart();
                updateMonthlyTotals();
                checkBudgetWarning();
            }
        }
    }
    private void loadExpensesFromStorage() {
        allExpenses = dataManager.loadExpenses();
        updateRecentExpenses();
        checkViewMoreButton();
        updateChart();
        updatePieChart();
        updateMonthlyTotals();
        checkBudgetWarning();
    }
    private void updateRecentExpenses() {
        if (allExpenses == null) {
            allExpenses = new ArrayList<>();
        }
        allExpenses.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));
        recentExpenses.clear();
        int maxRecent = Math.min(3, allExpenses.size());
        for (int i = 0; i < maxRecent; i++) {
            recentExpenses.add(allExpenses.get(i));
        }
        if (adapter == null) {
            adapter = new ExpenseAdapter(recentExpenses);
            adapter.setOnExpenseClickListener(new ExpenseAdapter.OnExpenseClickListener() {
                @Override
                public void onExpenseClick(Expense expense) {
                    Intent intent = new Intent(MainActivity.this, EditExpenseActivity.class);
                    intent.putExtra("EXPENSE_TO_EDIT", expense);
                    startActivityForResult(intent, 2);
                }
                @Override
                public void onExpenseLongClick(Expense expense) {
                    showDeleteDialog(expense);
                }
            });
            expensesRecyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();

            // Posunieme sa na začiatok, ak zoznam nie je prázdny
            if (!recentExpenses.isEmpty()) {
                expensesRecyclerView.scrollToPosition(0);
            }
        }
        updateEmptyState();
    }
    private void checkViewMoreButton() {
        if (btnViewMore != null && allExpenses != null) {
            if (allExpenses.size() > 2) {
                btnViewMore.setVisibility(View.VISIBLE);
            } else {
                btnViewMore.setVisibility(View.GONE);
            }
        }
    }
    private void showDeleteDialog(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Zmazať výdavok")
                .setMessage("Naozaj chcete zmazať výdavok '" + expense.getDescription() + "'?")
                .setPositiveButton("Áno", (dialog, which) -> deleteExpense(expense))
                .setNegativeButton("Nie", null)
                .show();
    }
    private void deleteExpense(Expense expense) {
        int position = -1;
        for (int i = 0; i < recentExpenses.size(); i++) {
            if (recentExpenses.get(i).getId().equals(expense.getId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            RecyclerView.ViewHolder viewHolder = expensesRecyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                View view = viewHolder.itemView;
                int finalPosition = position;
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
                fadeOut.setDuration(250);
                fadeOut.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setAlpha(1f);
                        allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
                        dataManager.deleteExpense(expense.getId());
                        recentExpenses.removeIf(e -> e.getId().equals(expense.getId()));
                        adapter.notifyItemRemoved(finalPosition);
                        adapter.notifyItemRangeChanged(finalPosition, recentExpenses.size());
                        checkViewMoreButton();
                        updateChart();
                        updatePieChart();
                        updateMonthlyTotals();
                        checkBudgetWarning();
                        updateEmptyState();
                    }
                });
                fadeOut.start();
            } else {
                allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
                dataManager.deleteExpense(expense.getId());
                recentExpenses.removeIf(e -> e.getId().equals(expense.getId()));
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, recentExpenses.size());
                checkViewMoreButton();
                updateChart();
                updatePieChart();
                updateMonthlyTotals();
                checkBudgetWarning();
                updateEmptyState();
            }
        } else {
            allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
            dataManager.deleteExpense(expense.getId());
            recentExpenses.removeIf(e -> e.getId().equals(expense.getId()));
            adapter.notifyDataSetChanged();
            checkViewMoreButton();
            updateChart();
            updatePieChart();
            updateMonthlyTotals();
            checkBudgetWarning();
            updateEmptyState();
        }
    }
    private void setupChart() {
        try {
            expenseChart.getDescription().setEnabled(false);
            expenseChart.setDrawGridBackground(false);
            expenseChart.setPinchZoom(false);
            expenseChart.setScaleEnabled(false);
            expenseChart.getLegend().setEnabled(false);
            expenseChart.setBackgroundColor(Color.TRANSPARENT);
            expenseChart.setExtraOffsets(10f, 20f, 10f, 10f);
            XAxis xAxis = expenseChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(7);
            xAxis.setTextColor(Color.parseColor("#9E9E9E"));
            xAxis.setTextSize(12f);
            xAxis.setYOffset(10f);
            expenseChart.getAxisLeft().setDrawGridLines(true);
            expenseChart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));
            expenseChart.getAxisLeft().setGridLineWidth(1f);
            expenseChart.getAxisLeft().setDrawAxisLine(false);
            expenseChart.getAxisLeft().setTextColor(Color.parseColor("#9E9E9E"));
            expenseChart.getAxisLeft().setTextSize(12f);
            expenseChart.getAxisLeft().setGranularity(1f);
            expenseChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.0f", value);
                }
            });
            expenseChart.getAxisRight().setEnabled(false);
            expenseChart.setVisibility(View.VISIBLE);
            updateChart();
        } catch (Exception e) {
            expenseChart.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }
    private void setupFilterButtons() {
        updateFilterButtonStates();
        btnDaily.setOnClickListener(v -> {
            currentFilter = "Deň";
            updateFilterButtonStates();
            updateChart();
            updatePieChart();
        });
        btnMonthly.setOnClickListener(v -> {
            currentFilter = "Mesiac";
            updateFilterButtonStates();
            updateChart();
            updatePieChart();
        });
        btnYearly.setOnClickListener(v -> {
            currentFilter = "Rok";
            updateFilterButtonStates();
            updateChart();
            updatePieChart();
        });
    }
    private void updateFilterButtonStates() {
        btnDaily.setBackgroundColor(Color.TRANSPARENT);
        btnMonthly.setBackgroundColor(Color.TRANSPARENT);
        btnYearly.setBackgroundColor(Color.TRANSPARENT);
        switch (currentFilter) {
            case "Deň":
                btnDaily.setBackgroundColor(Color.parseColor("#E8F5E8")); 
                break;
            case "Mesiac":
                btnMonthly.setBackgroundColor(Color.parseColor("#FFF3E0")); 
                break;
            case "Rok":
                btnYearly.setBackgroundColor(Color.parseColor("#E3F2FD")); 
                break;
        }
        updateStatusBarColor();
    }
    private String getChartColorForFilter() {
        switch (currentFilter) {
            case "Deň":
                return "#4CAF50"; 
            case "Mesiac":
                return "#FF9800"; 
            case "Rok":
                return "#2196F3"; 
            default:
                return "#4CAF50"; 
        }
    }
    private void updateStatusBarColor() {
        String colorString = getChartColorForFilter();
        int newColor = Color.parseColor(colorString);
        
        // Ak je farba rovnaká, neanimujeme
        if (currentStatusBarColor == newColor) {
            return;
        }
        
        // Vytvoríme animáciu pre plynulý prechod farieb
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), currentStatusBarColor, newColor);
        colorAnimation.setDuration(300); // 300ms animácia
        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            getWindow().setStatusBarColor(animatedColor);
        });
        
        colorAnimation.start();
        currentStatusBarColor = newColor;
    }
    private void updateChart() {
        try {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            Map<String, Float> aggregatedData = getAggregatedData();
            
            // Vytvoríme zoznam dát v správnom poradí (od najstaršieho po najnovší)
            List<String> orderedKeys = getOrderedKeys(aggregatedData);
            
            int index = 0;
            for (String key : orderedKeys) {
                Float value = aggregatedData.get(key);
                entries.add(new Entry(index, value != null ? value : 0f));
                labels.add(key);
                index++;
            }
            
            if (entries.isEmpty()) {
                entries.add(new Entry(0, 0));
                labels.add("Žiadne dáta");
            }
            boolean allZero = true;
            for (Entry entry : entries) {
                if (entry.getY() != 0f) {
                    allZero = false;
                    break;
                }
            }
            LineDataSet dataSet = new LineDataSet(entries, "Výdavky");
            String chartColor = getChartColorForFilter();
            dataSet.setColor(Color.parseColor(chartColor));
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setCubicIntensity(0.2f);
            dataSet.setLineWidth(2.5f);
            dataSet.setDrawCircles(true);
            dataSet.setCircleRadius(5f);
            dataSet.setCircleColor(Color.parseColor(chartColor));
            dataSet.setCircleHoleRadius(3f);
            dataSet.setCircleHoleColor(Color.WHITE);
            dataSet.setFillColor(Color.parseColor(chartColor));
            dataSet.setFillAlpha(40);
            dataSet.setDrawFilled(true);
            dataSet.setDrawValues(!allZero);
            dataSet.setValueTextSize(11f);
            dataSet.setValueTextColor(Color.parseColor("#616161"));
            LineData lineData = new LineData(dataSet);
            lineData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.0f€", value);
                }
            });
            expenseChart.setData(lineData);
            expenseChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            expenseChart.getAxisLeft().setAxisMinimum(0f);
            expenseChart.getAxisRight().setAxisMinimum(0f);
            expenseChart.animateX(600);
            expenseChart.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Map<String, Float> getAggregatedData() {
        Map<String, Float> aggregatedData = new HashMap<>();
        Date currentDate = new Date();
        for (Expense expense : allExpenses) {
            String key = getKeyForExpense(expense, currentDate);
            if (key != null) {
                aggregatedData.put(key, aggregatedData.getOrDefault(key, 0f) + (float) expense.getAmount());
            }
        }
        fillMissingPeriods(aggregatedData, currentDate);
        return aggregatedData;
    }
    private String getKeyForExpense(Expense expense, Date currentDate) {
        Calendar expenseCalendar = Calendar.getInstance();
        expenseCalendar.setTime(expense.getDate());
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        switch (currentFilter) {
            case "Deň":
                if (isWithinDays(expense.getDate(), currentDate, 4)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    return sdf.format(expense.getDate());
                }
                break;
            case "Mesiac":
                if (isWithinMonths(expense.getDate(), currentDate, 4)) {
                    return getSlovakMonthName(expenseCalendar.get(Calendar.MONTH));
                }
                break;
            case "Rok":
                if (isWithinYears(expense.getDate(), currentDate, 4)) {
                    return String.valueOf(expenseCalendar.get(Calendar.YEAR));
                }
                break;
        }
        return null;
    }
    private String getSlovakMonthName(int month) {
        String[] monthNames = {
            "Jan", "Feb", "Mar", "Apr", "Máj", "Jún",
            "Júl", "Aug", "Sep", "Okt", "Nov", "Dec"
        };
        return monthNames[month];
    }
    private boolean isWithinYears(Date expenseDate, Date currentDate, int years) {
        Calendar expenseCalendar = Calendar.getInstance();
        expenseCalendar.setTime(expenseDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        int yearsDiff = currentCalendar.get(Calendar.YEAR) - expenseCalendar.get(Calendar.YEAR);
        return yearsDiff >= 0 && yearsDiff < years;
    }
    private void fillMissingPeriods(Map<String, Float> data, Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        switch (currentFilter) {
            case "Deň":
                for (int i = 0; i <= 3; i++) {
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.setTime(currentDate);
                    dayCalendar.add(Calendar.DAY_OF_MONTH, -i);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    String key = sdf.format(dayCalendar.getTime());
                    data.putIfAbsent(key, 0f);
                }
                break;
            case "Mesiac":
                for (int i = 0; i <= 3; i++) {
                    Calendar monthCalendar = Calendar.getInstance();
                    monthCalendar.setTime(currentDate);
                    monthCalendar.add(Calendar.MONTH, -i);
                    String key = getSlovakMonthName(monthCalendar.get(Calendar.MONTH));
                    data.putIfAbsent(key, 0f);
                }
                break;
            case "Rok":
                for (int i = 0; i <= 3; i++) {
                    Calendar yearCalendar = Calendar.getInstance();
                    yearCalendar.setTime(currentDate);
                    yearCalendar.add(Calendar.YEAR, -i);
                    String key = String.valueOf(yearCalendar.get(Calendar.YEAR));
                    data.putIfAbsent(key, 0f);
                }
                break;
        }
    }
    
    private List<String> getOrderedKeys(Map<String, Float> data) {
        List<String> orderedKeys = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        
        switch (currentFilter) {
            case "Deň":
                // Od najstaršieho po najnovší (3 dni dozadu až dnes)
                for (int i = 3; i >= 0; i--) {
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.setTime(currentDate.getTime());
                    dayCalendar.add(Calendar.DAY_OF_MONTH, -i);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    String key = sdf.format(dayCalendar.getTime());
                    if (data.containsKey(key)) {
                        orderedKeys.add(key);
                    }
                }
                break;
            case "Mesiac":
                // Od najstaršieho po najnovší (3 mesiace dozadu až tento mesiac)
                for (int i = 3; i >= 0; i--) {
                    Calendar monthCalendar = Calendar.getInstance();
                    monthCalendar.setTime(currentDate.getTime());
                    monthCalendar.add(Calendar.MONTH, -i);
                    String key = getSlovakMonthName(monthCalendar.get(Calendar.MONTH));
                    if (data.containsKey(key)) {
                        orderedKeys.add(key);
                    }
                }
                break;
            case "Rok":
                // Od najstaršieho po najnovší (3 roky dozadu až tento rok)
                for (int i = 3; i >= 0; i--) {
                    Calendar yearCalendar = Calendar.getInstance();
                    yearCalendar.setTime(currentDate.getTime());
                    yearCalendar.add(Calendar.YEAR, -i);
                    String key = String.valueOf(yearCalendar.get(Calendar.YEAR));
                    if (data.containsKey(key)) {
                        orderedKeys.add(key);
                    }
                }
                break;
        }
        
        return orderedKeys;
    }
    private boolean isWithinDays(Date expenseDate, Date currentDate, int days) {
        Calendar expenseCalendar = Calendar.getInstance();
        expenseCalendar.setTime(expenseDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        currentCalendar.add(Calendar.DAY_OF_MONTH, -days);
        return expenseDate.after(currentCalendar.getTime()) || expenseDate.equals(currentCalendar.getTime());
    }
    private boolean isWithinMonths(Date expenseDate, Date currentDate, int months) {
        Calendar expenseCalendar = Calendar.getInstance();
        expenseCalendar.setTime(expenseDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        currentCalendar.add(Calendar.MONTH, -months);
        return expenseDate.after(currentCalendar.getTime()) || expenseDate.equals(currentCalendar.getTime());
    }
    private void updateMonthlyTotals() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        double monthlyTotal = 0;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (Expense expense : allExpenses) {
            Calendar expenseCalendar = Calendar.getInstance();
            expenseCalendar.setTime(expense.getDate());
            if (expenseCalendar.get(Calendar.MONTH) == currentMonth && 
                expenseCalendar.get(Calendar.YEAR) == currentYear) {
                monthlyTotal += expense.getAmount();
            }
        }
        monthlyTotalTextView.setText(String.format("%.2f €", monthlyTotal));
        double dailyAverage = monthlyTotal / Math.min(calendar.get(Calendar.DAY_OF_MONTH), daysInMonth);
        dailyAverageTextView.setText(String.format("%.2f €", dailyAverage));
        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);
        int lastMonthNum = lastMonth.get(Calendar.MONTH);
        int lastMonthYear = lastMonth.get(Calendar.YEAR);
        double lastMonthTotal = 0;
        for (Expense expense : allExpenses) {
            Calendar expenseCalendar = Calendar.getInstance();
            expenseCalendar.setTime(expense.getDate());
            if (expenseCalendar.get(Calendar.MONTH) == lastMonthNum && 
                expenseCalendar.get(Calendar.YEAR) == lastMonthYear) {
                lastMonthTotal += expense.getAmount();
            }
        }
        lastMonthTotalTextView.setText(String.format("%.2f €", lastMonthTotal));
        double change = monthlyTotal - lastMonthTotal;
        if (monthlyTotal == 0 && lastMonthTotal == 0) {
            monthlyChangeTextView.setText("–");
            monthlyChangeTextView.setTextColor(Color.parseColor("#666666"));
        } else if (monthlyTotal == 0) {
            monthlyChangeTextView.setText("Žiadne výdavky tento mesiac");
            monthlyChangeTextView.setTextColor(Color.parseColor("#666666"));
        } else if (change > 0) {
            monthlyChangeTextView.setText(String.format("+%.2f €", change));
            monthlyChangeTextView.setTextColor(Color.parseColor("#F44336"));
        } else if (change < 0) {
            monthlyChangeTextView.setText(String.format("-%.2f €", Math.abs(change)));
            monthlyChangeTextView.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            monthlyChangeTextView.setText("0.00 €");
            monthlyChangeTextView.setTextColor(Color.parseColor("#666666"));
        }
        checkBudgetWarning();
    }

    private void showBudgetDialog() {
        EditText etBudget = new EditText(this);
        etBudget.setHint("Zadajte mesačný limit (€)");
        etBudget.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        double currentBudget = dataManager.loadBudget();
        if (currentBudget > 0) {
            etBudget.setText(String.valueOf(currentBudget));
        }
        etBudget.setPadding(50, 30, 50, 30);
        new AlertDialog.Builder(this)
                .setTitle("Mesačný rozpočet")
                .setMessage("Nastavte mesačný limit výdavkov:")
                .setView(etBudget)
                .setPositiveButton("Uložiť", (dialog, which) -> {
                    String input = etBudget.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            double budget = Double.parseDouble(input);
                            dataManager.saveBudget(budget);
                            checkBudgetWarning();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Neplatná hodnota", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNeutralButton("Odstrániť limit", (dialog, which) -> {
                    dataManager.saveBudget(0.0);
                    checkBudgetWarning();
                })
                .setNegativeButton("Zrušiť", null)
                .show();
    }

    private void checkBudgetWarning() {
        if (tvBudgetStatus == null) return;
        double budget = dataManager.loadBudget();
        if (budget <= 0) {
            tvBudgetStatus.setVisibility(View.GONE);
            return;
        }
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        double monthlyTotal = 0;
        for (Expense expense : allExpenses) {
            Calendar expenseCalendar = Calendar.getInstance();
            expenseCalendar.setTime(expense.getDate());
            if (expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                    expenseCalendar.get(Calendar.YEAR) == currentYear) {
                monthlyTotal += expense.getAmount();
            }
        }
        double ratio = monthlyTotal / budget;
        if (ratio >= 1.0) {
            tvBudgetStatus.setVisibility(View.VISIBLE);
            tvBudgetStatus.setText("🚨 Prekročil si mesačný rozpočet!");
            tvBudgetStatus.setTextColor(Color.parseColor("#F44336"));
            tvBudgetStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else if (ratio >= 0.9) {
            tvBudgetStatus.setVisibility(View.VISIBLE);
            tvBudgetStatus.setText("⚠️ Blížiš sa k limitu!");
            tvBudgetStatus.setTextColor(Color.parseColor("#FF9800"));
            tvBudgetStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            tvBudgetStatus.setVisibility(View.GONE);
        }
    }

    private void updatePieChart() {
        if (pieChart == null) return;
        try {
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentYear = calendar.get(Calendar.YEAR);
            Map<String, Float> categoryTotals = new HashMap<>();
            for (Expense expense : allExpenses) {
                Calendar expenseCalendar = Calendar.getInstance();
                expenseCalendar.setTime(expense.getDate());
                if (expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                        expenseCalendar.get(Calendar.YEAR) == currentYear) {
                    String category = expense.getCategory();
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + (float) expense.getAmount());
                }
            }
            if (categoryTotals.isEmpty()) {
                pieChart.setVisibility(View.GONE);
                return;
            }
            Map<String, Integer> colorMap = new HashMap<>();
            colorMap.put("Potraviny", Color.parseColor("#4CAF50"));
            colorMap.put("Bývanie",   Color.parseColor("#2196F3"));
            colorMap.put("Doprava",   Color.parseColor("#FF9800"));
            colorMap.put("Zábava",    Color.parseColor("#9C27B0"));
            colorMap.put("Oblečenie", Color.parseColor("#E91E63"));
            colorMap.put("Zdravie",   Color.parseColor("#F44336"));
            colorMap.put("Jedlo",     Color.parseColor("#795548"));
            colorMap.put("Iné",       Color.parseColor("#607D8B"));
            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors  = new ArrayList<>();
            for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                Integer c = colorMap.get(entry.getKey());
                colors.add(c != null ? c : Color.parseColor("#607D8B"));
            }
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(11f);
            PieData pieData = new PieData(dataSet);
            pieData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format(Locale.getDefault(), "%.0f%%", value);
                }
            });
            pieChart.setData(pieData);
            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(50f);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.getDescription().setEnabled(false);
            pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
            pieChart.getLegend().setWordWrapEnabled(true);
            pieChart.setVisibility(View.VISIBLE);
            pieChart.invalidate();
        } catch (Exception e) {
            pieChart.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void updateEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(allExpenses.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView,
                                  @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder,
                                  @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < recentExpenses.size()) {
                    Expense expense = recentExpenses.get(position);
                    showDeleteDialog(expense);
                }
                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@androidx.annotation.NonNull Canvas c,
                                    @androidx.annotation.NonNull RecyclerView recyclerView,
                                    @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                ColorDrawable background = new ColorDrawable(Color.parseColor("#F44336"));
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                background.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(expensesRecyclerView);
    }
}
