package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataManager {
    
    private static final String PREFS_NAME = "expense_tracker_prefs";
    private static final String KEY_EXPENSES = "expenses";
    private static final String KEY_CATEGORIES = "categories";
    
    private SharedPreferences sharedPreferences;
    
    public DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Expense management
    public void saveExpenses(List<Expense> expenses) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Expense expense : expenses) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", expense.getId());
                jsonObject.put("amount", expense.getAmount());
                jsonObject.put("description", expense.getDescription());
                jsonObject.put("category", expense.getCategory());
                jsonObject.put("date", expense.getDate().getTime());
                jsonArray.put(jsonObject);
            }
            sharedPreferences.edit().putString(KEY_EXPENSES, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public List<Expense> loadExpenses() {
        String expensesJson = sharedPreferences.getString(KEY_EXPENSES, null);
        if (expensesJson == null) {
            return new ArrayList<>();
        }
        
        List<Expense> expenses = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(expensesJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Expense expense = new Expense();
                expense.setId(jsonObject.getString("id"));
                expense.setAmount(jsonObject.getDouble("amount"));
                expense.setDescription(jsonObject.getString("description"));
                expense.setCategory(jsonObject.getString("category"));
                expense.setDate(new Date(jsonObject.getLong("date")));
                expenses.add(expense);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return expenses;
    }
    
    public void addExpense(Expense expense) {
        List<Expense> expenses = loadExpenses();
        expenses.add(0, expense); // Add to beginning
        saveExpenses(expenses);
    }
    
    public void updateExpense(Expense updatedExpense) {
        List<Expense> expenses = loadExpenses();
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(updatedExpense.getId())) {
                expenses.set(i, updatedExpense);
                break;
            }
        }
        saveExpenses(expenses);
    }
    
    public void deleteExpense(String expenseId) {
        List<Expense> expenses = loadExpenses();
        expenses.removeIf(expense -> expense.getId().equals(expenseId));
        saveExpenses(expenses);
    }
    
    // Category management
    public void saveCategories(List<String> categories) {
        JSONArray jsonArray = new JSONArray();
        for (String category : categories) {
            jsonArray.put(category);
        }
        sharedPreferences.edit().putString(KEY_CATEGORIES, jsonArray.toString()).apply();
    }
    
    public List<String> loadCategories() {
        String categoriesJson = sharedPreferences.getString(KEY_CATEGORIES, null);
        if (categoriesJson == null) {
            // Return default categories if none saved
            return new ArrayList<>(Arrays.asList(
                "Potraviny", "Bývanie", "Doprava", "Zábava",
                "Oblečenie", "Zdravie", "Jedlo", "Iné"
            ));
        }
        
        List<String> categories = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(categoriesJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                categories.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public void addCategory(String category) {
        List<String> categories = loadCategories();
        if (!categories.contains(category)) {
            categories.add(category);
            saveCategories(categories);
        }
    }
    
    public void deleteCategory(String category) {
        List<String> categories = loadCategories();
        categories.remove(category);
        saveCategories(categories);
    }
    
    // Clear all data
    public void clearAllData() {
        sharedPreferences.edit().clear().apply();
    }
}
