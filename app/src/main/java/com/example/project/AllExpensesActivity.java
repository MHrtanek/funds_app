package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllExpensesActivity extends AppCompatActivity {

    private RecyclerView allExpensesRecyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private TextView totalExpensesTextView, topExpenseTextView;
    private EditText etSearch;
    private Spinner spinnerFilterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_all_expenses);

        // Initialize views
        allExpensesRecyclerView = findViewById(R.id.allExpensesRecyclerView);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);
        topExpenseTextView = findViewById(R.id.topExpenseTextView);
        etSearch = findViewById(R.id.etSearch);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnExport = findViewById(R.id.btnExport);

        // Get all expenses from intent
        Expense[] expensesArray = (Expense[]) getIntent().getSerializableExtra("ALL_EXPENSES");
        if (expensesArray != null) {
            allExpenses = new ArrayList<>(Arrays.asList(expensesArray));
            filteredExpenses = new ArrayList<>(allExpenses);
        }

        // Setup RecyclerView
        adapter = new ExpenseAdapter(filteredExpenses);
        allExpensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allExpensesRecyclerView.setAdapter(adapter);
        
        // Set click listener for editing and deleting expenses
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

        // Setup search and filter
        setupSearchAndFilter();

        // Calculate and display total
        updateTotalExpenses();

        // Back button functionality
        btnBack.setOnClickListener(v -> finish());
        
        // Export button functionality
        btnExport.setOnClickListener(v -> exportToCSV());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
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
                
                // Update filtered list
                filterExpenses();
            }
        }
    }

    private void setupSearchAndFilter() {
        // Setup category filter spinner
        List<String> categories = new ArrayList<>();
        categories.add("Všetky kategórie");
        categories.addAll(Arrays.asList("Potraviny", "Bývanie", "Doprava", "Zábava", "Oblečenie", "Zdravie", "Jedlo", "Iné"));
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(categoryAdapter);
        
        // Setup search text watcher
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
        
        // Setup category filter
        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
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
        
        adapter.notifyDataSetChanged();
        updateTotalExpenses();
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
        
        totalExpensesTextView.setText(String.format("%.2f €", total));
        topExpenseTextView.setText(String.format("%.2f €", maxExpense));
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
        
        // Update filtered list
        filterExpenses();
    }

    private void exportToCSV() {
        try {
            // Create CSV content
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
            
            // Create file
            String fileName = "vydavky_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);
            
            // Write to file
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent.toString());
            writer.close();
            
            Toast.makeText(this, "CSV súbor uložený do Downloads: " + fileName, Toast.LENGTH_LONG).show();
            
            // Share file
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