package com.example.project;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_add_expense);
        dataManager = new DataManager(this);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        calendarView = findViewById(R.id.calendarView);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        selectedDate = Calendar.getInstance();
        setupCategorySpinner();
        setupEnterListener();
        setupCalendarRestrictions();
        btnSave.setOnClickListener(v -> saveExpense());
        btnManageCategories.setOnClickListener(v -> openCategoryManagement());
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
    
    private void setupEnterListener() {
        etDescription.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Iba zatvoríme klávesnicu, neuložíme výdavok
                    etDescription.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etDescription.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }
    
    private void setupCalendarRestrictions() {
        // Nastavíme maximálny dátum na dnešný deň
        Calendar today = Calendar.getInstance();
        long maxDate = today.getTimeInMillis();
        calendarView.setMaxDate(maxDate);
        
        // Nastavíme minimálny dátum na pred 1 rok (voliteľné)
        Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.add(Calendar.YEAR, -1);
        long minDate = oneYearAgo.getTimeInMillis();
        calendarView.setMinDate(minDate);
    }
    private void openCategoryManagement() {
        Intent intent = new Intent(this, CategoryManagementActivity.class);
        startActivityForResult(intent, 100); 
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            setupCategorySpinner(); 
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
        Expense newExpense = new Expense(amount, description, category, selectedDateObj);
        dataManager.addExpense(newExpense);
        setResult(RESULT_OK);
        finish();
    }
}
