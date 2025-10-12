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

public class EditExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel, btnSelectDate;
    private Calendar selectedDate;
    private Expense expenseToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_edit_expense);

        // Get expense from intent
        expenseToEdit = (Expense) getIntent().getSerializableExtra("EXPENSE_TO_EDIT");
        if (expenseToEdit == null) {
            Toast.makeText(this, "Chyba: Výdavok nebol nájdený", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Nájdeme elementy
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        
        // Nastavíme aktuálne hodnoty
        etAmount.setText(String.valueOf(expenseToEdit.getAmount()));
        etDescription.setText(expenseToEdit.getDescription());
        
        selectedDate = Calendar.getInstance();
        selectedDate.setTime(expenseToEdit.getDate());
        updateDateButtonText();

        // Nastavíme kategórie pre Spinner
        setupCategorySpinner();

        // Nastavíme aktuálnu kategóriu
        List<String> categories = Arrays.asList("Potraviny", "Bývanie", "Doprava", "Zábava", "Oblečenie", "Zdravie", "Jedlo", "Iné");
        int categoryIndex = categories.indexOf(expenseToEdit.getCategory());
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex);
        }

        // Nastavíme klik listenery
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> finish());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList(
                "Potraviny", "Bývanie", "Doprava", "Zábava",
                "Oblečenie", "Zdravie", "Jedlo", "Iné"
        );

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

    private void saveExpense() {
        String amountText = etAmount.getText().toString();
        String description = etDescription.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (amountText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Prosím vyplňte všetky polia", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            Date selectedDateObj = selectedDate.getTime();

            // Aktualizujeme výdavok
            expenseToEdit.setAmount(amount);
            expenseToEdit.setDescription(description);
            expenseToEdit.setCategory(category);
            expenseToEdit.setDate(selectedDateObj);

            // Poslať aktualizovaný výdavok späť
            Intent resultIntent = new Intent();
            resultIntent.putExtra("EDITED_EXPENSE", expenseToEdit);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Neplatná suma", Toast.LENGTH_SHORT).show();
        }
    }
}
