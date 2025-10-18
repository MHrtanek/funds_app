package com.example.project;



import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel, btnSelectDate, btnManageCategories;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_add_expense);

        // Nájdeme elementy
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        
        // Nastavíme dnešný dátum ako predvolený
        selectedDate = Calendar.getInstance();
        updateDateButtonText();

        // Nastavíme kategórie pre Spinner
        setupCategorySpinner();

        // Nastavíme klik listenery
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> finish());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnManageCategories.setOnClickListener(v -> openCategoryManagement());
    }

    private void setupCategorySpinner() {
        DataManager dataManager = new DataManager(this);
        List<String> categories = dataManager.loadCategories();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void updateDateButtonText() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        btnSelectDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButtonText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void openCategoryManagement() {
        Intent intent = new Intent(this, CategoryManagementActivity.class);
        startActivityForResult(intent, 100); // Použijeme requestCode 100 pre správu kategórií
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Ak sa vrátili z CategoryManagementActivity, obnovíme kategórie
        if (requestCode == 100 && resultCode == RESULT_OK) {
            setupCategorySpinner(); // Obnovíme spinner s novými kategóriami
        }
    }

    private void saveExpense() {
        String amountText = etAmount.getText().toString();
        String description = etDescription.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (amountText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Prosim vyplnte všetky polia ", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountText);
        Date selectedDateObj = selectedDate.getTime();

        // Vytvoriť nový výdavok
        Expense newExpense = new Expense(amount, description, category, selectedDateObj);

        // Poslať výdavok späť do MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("AMOUNT", amount);
        resultIntent.putExtra("DESCRIPTION", description);
        resultIntent.putExtra("CATEGORY", category);
        resultIntent.putExtra("DATE", selectedDateObj.getTime()); // uložiť ako timestamp

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}