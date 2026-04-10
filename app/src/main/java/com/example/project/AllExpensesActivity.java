package com.example.project;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class AllExpensesActivity extends AppCompatActivity {
    private RecyclerView allExpensesRecyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private TextView totalExpensesTextView, topExpenseTextView;
    private TextView tvExpenseCount, tvAverageExpense, tvTopCategory;
    private EditText etSearch;
    private Spinner spinnerFilterCategory, spinnerSort;
    private String currentSortType = "Najnovšie";
    private DataManager dataManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_all_expenses);
        dataManager = new DataManager(this);
        allExpenses = dataManager.loadExpenses();
        filteredExpenses = new ArrayList<>(allExpenses);
        allExpensesRecyclerView = findViewById(R.id.allExpensesRecyclerView);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);
        topExpenseTextView = findViewById(R.id.topExpenseTextView);
        tvExpenseCount = findViewById(R.id.tvExpenseCount);
        tvAverageExpense = findViewById(R.id.tvAverageExpense);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        etSearch = findViewById(R.id.etSearch);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        spinnerSort = findViewById(R.id.spinnerSort);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnExport = findViewById(R.id.btnExport);
        adapter = new ExpenseAdapter(filteredExpenses);
        allExpensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allExpensesRecyclerView.setAdapter(adapter);
        setupSwipeToDelete();
        adapter.setOnExpenseClickListener(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                Intent intent = new Intent(AllExpensesActivity.this, EditExpenseActivity.class);
                intent.putExtra("EXPENSE_TO_EDIT", expense);
                startActivityForResult(intent, 1);
            }
            @Override
            public void onExpenseLongClick(Expense expense) {
                showDeleteDialog(expense);
            }
        });
        setupSearchAndFilter();
        updateTotalExpenses();
        btnBack.setOnClickListener(v -> finish());
        btnExport.setOnClickListener(v -> exportToCSV());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Expense editedExpense = (Expense) data.getSerializableExtra("EDITED_EXPENSE");
            if (editedExpense != null) {
                for (int i = 0; i < allExpenses.size(); i++) {
                    if (allExpenses.get(i).getId().equals(editedExpense.getId())) {
                        allExpenses.set(i, editedExpense);
                        break;
                    }
                }
                filterExpenses();
            }
        }
    }
    private void setupSearchAndFilter() {
        List<String> categories = new ArrayList<>();
        categories.add("Všetky kategórie");
        categories.addAll(dataManager.loadCategories());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(categoryAdapter);

        List<String> sortOptions = Arrays.asList(
                "Najnovšie", "Najstaršie", "Najvyššia suma", "Najnižšia suma", "Kategória A-Z");
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filterExpenses();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                currentSortType = sortOptions.get(position);
                filterExpenses();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void filterExpenses() {
        String searchText = etSearch.getText().toString().toLowerCase();
        String selectedCategory = spinnerFilterCategory.getSelectedItem().toString();
        filteredExpenses.clear();
        for (Expense expense : allExpenses) {
            boolean matchesSearch = searchText.isEmpty() || 
                    expense.getDescription().toLowerCase().contains(searchText);
            boolean matchesCategory = selectedCategory.equals("Všetky kategórie") || 
                    expense.getCategory().equals(selectedCategory);
            if (matchesSearch && matchesCategory) {
                filteredExpenses.add(expense);
            }
        }
        sortExpenses(currentSortType);
        adapter.notifyDataSetChanged();
        updateTotalExpenses();
    }

    private void sortExpenses(String sortType) {
        switch (sortType) {
            case "Najnovšie":
                Collections.sort(filteredExpenses, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                break;
            case "Najstaršie":
                Collections.sort(filteredExpenses, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
                break;
            case "Najvyššia suma":
                Collections.sort(filteredExpenses, (e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
                break;
            case "Najnižšia suma":
                Collections.sort(filteredExpenses, (e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
                break;
            case "Kategória A-Z":
                Collections.sort(filteredExpenses, (e1, e2) -> e1.getCategory().compareTo(e2.getCategory()));
                break;
        }
    }

    private void updateTotalExpenses() {
        double total = 0;
        double maxExpense = 0;
        for (Expense expense : filteredExpenses) {
            total += expense.getAmount();
            if (expense.getAmount() > maxExpense) {
                maxExpense = expense.getAmount();
            }
        }
        totalExpensesTextView.setText(String.format("%d výdavkov · %.2f €", filteredExpenses.size(), total));
        topExpenseTextView.setText(String.format("%.2f €", maxExpense));

        tvExpenseCount.setText(String.valueOf(filteredExpenses.size()));
        if (filteredExpenses.size() > 0) {
            tvAverageExpense.setText(String.format("%.2f €", total / filteredExpenses.size()));
        } else {
            tvAverageExpense.setText("0.00 €");
        }

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : filteredExpenses) {
            categoryTotals.put(expense.getCategory(),
                    categoryTotals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
        }
        String topCategory = "–";
        double maxCategoryTotal = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > maxCategoryTotal) {
                maxCategoryTotal = entry.getValue();
                topCategory = entry.getKey();
            }
        }
        if (maxCategoryTotal > 0) {
            tvTopCategory.setText(topCategory + " (" + String.format("%.2f €", maxCategoryTotal) + ")");
        } else {
            tvTopCategory.setText("–");
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
                if (position >= 0 && position < filteredExpenses.size()) {
                    Expense expense = filteredExpenses.get(position);
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
        itemTouchHelper.attachToRecyclerView(allExpensesRecyclerView);
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
        dataManager.deleteExpense(expense.getId());
        allExpenses.removeIf(e -> e.getId().equals(expense.getId()));
        filterExpenses();
    }
    private void exportToCSV() {
        try {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Dátum,Suma,Kategória,Popis\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (Expense expense : filteredExpenses) {
                csvContent.append(dateFormat.format(expense.getDate()))
                         .append(",")
                         .append(expense.getAmount())
                         .append(",")
                         .append("\"").append(expense.getCategory()).append("\"")
                         .append(",")
                         .append("\"").append(expense.getDescription()).append("\"")
                         .append("\n");
            }
            String fileName = "vydavky_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent.toString());
            writer.close();
            Toast.makeText(this, "CSV súbor uložený do Downloads: " + fileName, Toast.LENGTH_LONG).show();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, 
                    getApplicationContext().getPackageName() + ".fileprovider", csvFile));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Zdieľať CSV súbor"));
        } catch (IOException e) {
            Toast.makeText(this, "Chyba pri exporte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
