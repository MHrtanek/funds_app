package com.example.project;

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
    private List<Expense> allExpenses = new ArrayList<>(); // Store all expenses
    private List<Expense> recentExpenses = new ArrayList<>(); // Show only recent 3
    private ExpenseAdapter adapter; // Deklarácia adapteru
    private String currentFilter = "Deň"; // Default filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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

        // Nastav dátum
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateTextView.setText(currentDate);

        // INICIALIZUJ ADAPTER A RECYCLERVIEW - PRIDAJ TOTO
        adapter = new ExpenseAdapter(recentExpenses);
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expensesRecyclerView.setAdapter(adapter);
        
        // Set click listener for editing and deleting expenses
        adapter.setOnExpenseClickListener(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                Intent intent = new Intent(MainActivity.this, EditExpenseActivity.class);
                intent.putExtra("EXPENSE_TO_EDIT", expense);
                startActivityForResult(intent, 2); // Use request code 2 for editing
            }
            
            @Override
            public void onExpenseLongClick(Expense expense) {
                showDeleteDialog(expense);
            }
        });

        // Setup chart
        setupChart();

        // Setup filter buttons
        setupFilterButtons();
        
        // Update monthly totals
        updateMonthlyTotals();

        // BTN functionality adding new expense.
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, 1); // Zmena na startActivityForResult
        });

        // BTN view more
        btnViewMore.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllExpensesActivity.class);
            intent.putExtra("ALL_EXPENSES", allExpenses.toArray(new Expense[0]));
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Adding new expense
            double amount = data.getDoubleExtra("AMOUNT", 0);
            String description = data.getStringExtra("DESCRIPTION");
            String category = data.getStringExtra("CATEGORY");
            long dateTimestamp = data.getLongExtra("DATE", System.currentTimeMillis());

            Date date = new Date(dateTimestamp);
            Expense newExpense = new Expense(amount, description, category, date);

            // Pridať do všetkých výdavkov na začiatok
            allExpenses.add(0, newExpense);
            
            // Obmedziť recent výdavky na maximálne 2 (pred pridaním nového)
            if (recentExpenses.size() >= 3) {
                recentExpenses = new ArrayList<>(recentExpenses.subList(0, 2));
            }
            
            // Pridať nový výdavok na začiatok recent zoznamu
            recentExpenses.add(0, newExpense);

            adapter.notifyDataSetChanged(); // Teraz už adapter existuje
            checkViewMoreButton();
            updateChart(); // Update chart when new expense is added
            updateMonthlyTotals(); // Update monthly totals
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            // Editing existing expense
            Expense editedExpense = (Expense) data.getSerializableExtra("EDITED_EXPENSE");
            if (editedExpense != null) {
                // Update in allExpenses
                for (int i = 0; i < allExpenses.size(); i++) {
                    if (allExpenses.get(i).getId().equals(editedExpense.getId())) {
                        allExpenses.set(i, editedExpense);
                        break;
                    }
                }
                
                // Update in recentExpenses
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

    private void checkViewMoreButton() {
        if (allExpenses.size() > 2) {
            btnViewMore.setVisibility(View.VISIBLE);
        } else {
            btnViewMore.setVisibility(View.GONE);
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
        // Remove from allExpenses
        allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
        
        // Remove from recentExpenses
        recentExpenses.removeIf(e -> e.getId().equals(expense.getId()));
        
        // Update UI
        adapter.notifyDataSetChanged();
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

            // Set chart background to white/light
            expenseChart.setBackgroundColor(Color.WHITE);
            expenseChart.setGridBackgroundColor(Color.LTGRAY);

            // Setup X-axis
            XAxis xAxis = expenseChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(7);
            xAxis.setTextColor(Color.BLACK);
            xAxis.setTextSize(12f);
            xAxis.setAxisLineColor(Color.BLACK);
            
            // Add extra padding at bottom for month labels
            expenseChart.setExtraOffsets(0f, 0f, 0f, 20f);

            // Setup Y-axis
            expenseChart.getAxisLeft().setDrawGridLines(true);
            expenseChart.getAxisLeft().setGridColor(Color.LTGRAY);
            expenseChart.getAxisLeft().setTextColor(Color.BLACK);
            expenseChart.getAxisLeft().setTextSize(12f);
            expenseChart.getAxisLeft().setAxisLineColor(Color.BLACK);
            expenseChart.getAxisRight().setEnabled(false);

            // Make chart visible
            expenseChart.setVisibility(View.VISIBLE);

            updateChart();
        } catch (Exception e) {
            // If chart setup fails, make it invisible
            expenseChart.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void setupFilterButtons() {
        // Set initial button state
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
        // Reset all buttons to default style
        btnDaily.setBackgroundColor(Color.TRANSPARENT);
        btnMonthly.setBackgroundColor(Color.TRANSPARENT);
        btnYearly.setBackgroundColor(Color.TRANSPARENT);

        // Highlight selected button with filter-specific color
        switch (currentFilter) {
            case "Deň":
                btnDaily.setBackgroundColor(Color.parseColor("#E8F5E8")); // Light green
                break;
            case "Mesiac":
                btnMonthly.setBackgroundColor(Color.parseColor("#FFF3E0")); // Light orange
                break;
            case "Rok":
                btnYearly.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue
                break;
        }
    }

    private String getChartColorForFilter() {
        switch (currentFilter) {
            case "Deň":
                return "#4CAF50"; // Green
            case "Mesiac":
                return "#FF9800"; // Orange
            case "Rok":
                return "#2196F3"; // Blue
            default:
                return "#4CAF50"; // Default green
        }
    }

    private void updateChart() {
        try {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            Map<String, Float> aggregatedData = getAggregatedData();

            int index = 0;
            for (Map.Entry<String, Float> entry : aggregatedData.entrySet()) {
                entries.add(new Entry(index, entry.getValue()));
                labels.add(entry.getKey());
                index++;
            }

            if (entries.isEmpty()) {
                // Add a dummy entry to show something
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
            dataSet.setDrawFilled(true); // This makes it an area chart
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
            
            // Set Y-axis to start from 0 and not go negative
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

        // Fill missing periods with 0 values
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
                // Show last 4 days
                if (isWithinDays(expense.getDate(), currentDate, 4)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    return sdf.format(expense.getDate());
                }
                break;
            case "Mesiac":
                // Show last 4 months with Slovak month names
                if (isWithinMonths(expense.getDate(), currentDate, 4)) {
                    return getSlovakMonthName(expenseCalendar.get(Calendar.MONTH));
                }
                break;
            case "Rok":
                // Show last 4 years
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
        
        // Calculate daily average
        double dailyAverage = monthlyTotal / daysInMonth;
        dailyAverageTextView.setText(String.format("%.2f €", dailyAverage));
        
        // Calculate last month total
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
        
        // Calculate change
        double change = monthlyTotal - lastMonthTotal;
        String changeText = String.format("%.2f €", Math.abs(change));
        if (change > 0) {
            changeText = "+" + changeText;
            monthlyChangeTextView.setTextColor(Color.parseColor("#F44336")); // Red for increase
        } else if (change < 0) {
            changeText = "-" + changeText;
            monthlyChangeTextView.setTextColor(Color.parseColor("#4CAF50")); // Green for decrease
        } else {
            monthlyChangeTextView.setTextColor(Color.parseColor("#666666")); // Gray for no change
        }
        monthlyChangeTextView.setText(changeText);
    }
}