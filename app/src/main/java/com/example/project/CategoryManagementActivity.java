package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryDeleteListener {
    
    private EditText etNewCategory;
    private Button btnAddCategory, btnBack;
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private List<String> categories;
    private DataManager dataManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize data manager
        dataManager = new DataManager(this);
        
        // Hide the action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_category_management);
        
        // Initialize views
        etNewCategory = findViewById(R.id.etNewCategory);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        
        // Initialize categories list
        initializeCategories();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        btnAddCategory.setOnClickListener(v -> addNewCategory());
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void initializeCategories() {
        // Load categories from storage
        categories = dataManager.loadCategories();
    }
    
    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories, this);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }
    
    private void addNewCategory() {
        String newCategoryName = etNewCategory.getText().toString().trim();
        
        if (newCategoryName.isEmpty()) {
            Toast.makeText(this, "Zadajte názov kategórie", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (categories.contains(newCategoryName)) {
            Toast.makeText(this, "Kategória už existuje", Toast.LENGTH_SHORT).show();
            return;
        }
        
        categories.add(newCategoryName);
        dataManager.saveCategories(categories); // Save to storage
        categoryAdapter.notifyItemInserted(categories.size() - 1);
        etNewCategory.setText("");
        Toast.makeText(this, "Kategória '" + newCategoryName + "' bola pridaná", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onCategoryDelete(String category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Odstrániť kategóriu")
                .setMessage("Naozaj chcete odstrániť kategóriu '" + category + "'?")
                .setPositiveButton("Odstrániť", (dialog, which) -> {
                    int position = categories.indexOf(category);
                    if (position != -1) {
                        categories.remove(position);
                        dataManager.saveCategories(categories); // Save to storage
                        categoryAdapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Kategória '" + category + "' bola odstránená", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Zrušiť", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Set button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
    }
    
    @Override
    public void finish() {
        // Return updated categories to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("CATEGORIES", categories.toArray(new String[0]));
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }
}
