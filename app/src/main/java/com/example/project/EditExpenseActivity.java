package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
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
    private Button btnSave;
    private CalendarView calendarView;
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

        // Set status bar color to orange for edit activity
        getWindow().setStatusBarColor(Color.parseColor("#FFFF9800"));

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
        calendarView = findViewById(R.id.calendarView);
        
        // Nastavíme aktuálne hodnoty
        etAmount.setText(String.valueOf(expenseToEdit.getAmount()));
        etDescription.setText(expenseToEdit.getDescription());
        
        selectedDate = Calendar.getInstance();
        selectedDate.setTime(expenseToEdit.getDate());

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
        
        // Setup calendar view
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
        });
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
