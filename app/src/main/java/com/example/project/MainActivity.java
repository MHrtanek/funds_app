package com.example.project;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
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
    private Button btnAdd, btnViewMore, btnDaily, btnYearly, btnMonthly;
    private LineChart expenseChart;
    private TextView monthlyTotalTextView, dailyAverageTextView, lastMonthTotalTextView, monthlyChangeTextView;
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
        checkViewMoreButton();
        updateChart();
        updateMonthlyTotals();
    }
    @Override
    protected void onResume() {
        super.onResume();
        allExpenses = dataManager.loadExpenses();
        updateRecentExpenses();
        checkViewMoreButton();
        updateChart();
        updateMonthlyTotals();
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
                updateMonthlyTotals();
            }
        }
    }
    private void loadExpensesFromStorage() {
        allExpenses = dataManager.loadExpenses();
        updateRecentExpenses();
        checkViewMoreButton();
        updateChart();
        updateMonthlyTotals();
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
        }
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
        // Nájdeme pozíciu výdavku v zozname
        int position = -1;
        for (int i = 0; i < recentExpenses.size(); i++) {
            if (recentExpenses.get(i).getId().equals(expense.getId())) {
                position = i;
                break;
            }
        }
        
        // Ak sme našli pozíciu, použijeme animáciu zmiznutia
        if (position != -1) {
            // Najprv odstránime z dát
            allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
            dataManager.deleteExpense(expense.getId());
            recentExpenses.removeIf(e -> e.getId().equals(expense.getId()));
            
            // Potom animujeme zmiznutie
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, recentExpenses.size());
        } else {
            // Ak sme nenašli pozíciu, odstránime z dát a aktualizujeme adapter
            allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
            dataManager.deleteExpense(expense.getId());
            recentExpenses.removeIf(e -> e.getId().equals(expense.getId()));
            adapter.notifyDataSetChanged();
        }
        
        checkViewMoreButton();
        updateChart();
        updateMonthlyTotals();
    }
    private void setupChart() {
        try {
            expenseChart.getDescription().setEnabled(false);
            expenseChart.setDrawGridBackground(false);
            expenseChart.setPinchZoom(false);
            expenseChart.setScaleEnabled(false);
            expenseChart.getLegend().setEnabled(false);
            expenseChart.setBackgroundColor(Color.WHITE);
            expenseChart.setGridBackgroundColor(Color.LTGRAY);
            XAxis xAxis = expenseChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(7);
            xAxis.setTextColor(Color.BLACK);
            xAxis.setTextSize(12f);
            xAxis.setAxisLineColor(Color.BLACK);
            expenseChart.setExtraOffsets(0f, 0f, 0f, 20f);
            expenseChart.getAxisLeft().setDrawGridLines(true);
            expenseChart.getAxisLeft().setGridColor(Color.LTGRAY);
            expenseChart.getAxisLeft().setTextColor(Color.BLACK);
            expenseChart.getAxisLeft().setTextSize(12f);
            expenseChart.getAxisLeft().setAxisLineColor(Color.BLACK);
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
        });
        btnMonthly.setOnClickListener(v -> {
            currentFilter = "Mesiac";
            updateFilterButtonStates();
            updateChart();
        });
        btnYearly.setOnClickListener(v -> {
            currentFilter = "Rok";
            updateFilterButtonStates();
            updateChart();
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
            LineDataSet dataSet = new LineDataSet(entries, "Výdavky");
            String chartColor = getChartColorForFilter();
            dataSet.setColor(Color.parseColor(chartColor));
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(12f);
            dataSet.setLineWidth(3f);
            dataSet.setCircleRadius(6f);
            dataSet.setCircleColor(Color.parseColor(chartColor));
            dataSet.setFillColor(Color.parseColor(chartColor));
            dataSet.setFillAlpha(100);
            dataSet.setDrawFilled(true); 
            dataSet.setDrawValues(true);
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
            expenseChart.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Map<String, Float> getAggregatedData() {
        Map<String, Float> aggregatedData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
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
    private boolean isWithinWeeks(Date expenseDate, Date currentDate, int weeks) {
        Calendar expenseCalendar = Calendar.getInstance();
        expenseCalendar.setTime(expenseDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        currentCalendar.add(Calendar.WEEK_OF_YEAR, -weeks);
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
        double dailyAverage = monthlyTotal / daysInMonth;
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
        String changeText = String.format("%.2f €", Math.abs(change));
        if (change > 0) {
            changeText = "+" + changeText;
            monthlyChangeTextView.setTextColor(Color.parseColor("#F44336")); 
        } else if (change < 0) {
            changeText = "-" + changeText;
            monthlyChangeTextView.setTextColor(Color.parseColor("#4CAF50")); 
        } else {
            monthlyChangeTextView.setTextColor(Color.parseColor("#666666")); 
        }
        monthlyChangeTextView.setText(changeText);
    }
}
