package com.example.project;



import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private Button btnSave, btnManageCategories;
    private CalendarView calendarView;
    private Calendar selectedDate;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_add_expense);

        // Initialize DataManager
        dataManager = new DataManager(this);

        // Nájdeme elementy
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        calendarView = findViewById(R.id.calendarView);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        
        // Nastavíme dnešný dátum ako predvolený
        selectedDate = Calendar.getInstance();

        // Nastavíme kategórie pre Spinner
        setupCategorySpinner();

        // Nastavíme klik listenery
        btnSave.setOnClickListener(v -> saveExpense());
        btnManageCategories.setOnClickListener(v -> openCategoryManagement());
        
        // Setup calendar view
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
        });
    }

    private void setupCategorySpinner() {
        List<String> categories = dataManager.loadCategories();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
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

        // Uložiť do DataManager
        dataManager.addExpense(newExpense);

        // Vrátiť späť do MainActivity
        setResult(RESULT_OK);
        finish();
    }
}