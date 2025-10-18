package com.example.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseSearchHelper {
    
    private List<Expense> allExpenses;
    private List<Expense> filteredExpenses;
    
    public ExpenseSearchHelper(List<Expense> allExpenses) {
        this.allExpenses = allExpenses;
        this.filteredExpenses = new ArrayList<>(allExpenses);
    }
    
    /**
     * Filter expenses based on search text and category
     * Optimized search with early termination and lowercase comparison
     */
    public List<Expense> filterExpenses(String searchText, String selectedCategory) {
        if (allExpenses == null || allExpenses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Prepare search text once
        String normalizedSearchText = searchText != null ? 
            searchText.toLowerCase(Locale.getDefault()).trim() : "";
        
        boolean searchEmpty = normalizedSearchText.isEmpty();
        boolean categoryAll = selectedCategory == null || 
            selectedCategory.equals("Všetky kategórie") || 
            selectedCategory.equals(Constants.EXPENSE_CATEGORIES.get(0));
        
        // Early return for no filters
        if (searchEmpty && categoryAll) {
            filteredExpenses = new ArrayList<>(allExpenses);
            return filteredExpenses;
        }
        
        List<Expense> result = new ArrayList<>();
        
        // Optimized iteration with early termination
        for (Expense expense : allExpenses) {
            boolean matchesSearch = searchEmpty || 
                expense.getDescription().toLowerCase(Locale.getDefault()).contains(normalizedSearchText);
            
            boolean matchesCategory = categoryAll || 
                expense.getCategory().equals(selectedCategory);
            
            if (matchesSearch && matchesCategory) {
                result.add(expense);
            }
        }
        
        filteredExpenses = result;
        return filteredExpenses;
    }
    
    /**
     * Fast search by description only (for autocomplete suggestions)
     */
    public List<Expense> searchByDescription(String searchText) {
        if (allExpenses == null || allExpenses.isEmpty() || 
            searchText == null || searchText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String normalizedSearchText = searchText.toLowerCase(Locale.getDefault()).trim();
        List<Expense> result = new ArrayList<>();
        
        for (Expense expense : allExpenses) {
            if (expense.getDescription().toLowerCase(Locale.getDefault())
                .contains(normalizedSearchText)) {
                result.add(expense);
                
                // Limit results for performance
                if (result.size() >= 50) {
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Filter by category only (optimized)
     */
    public List<Expense> filterByCategory(String category) {
        if (allExpenses == null || allExpenses.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (category == null || category.equals("Všetky kategórie")) {
            filteredExpenses = new ArrayList<>(allExpenses);
            return filteredExpenses;
        }
        
        List<Expense> result = new ArrayList<>();
        for (Expense expense : allExpenses) {
            if (expense.getCategory().equals(category)) {
                result.add(expense);
            }
        }
        
        filteredExpenses = result;
        return filteredExpenses;
    }
    
    /**
     * Get unique categories from expenses (cached for performance)
     */
    public List<String> getAvailableCategories() {
        List<String> categories = new ArrayList<>();
        for (Expense expense : allExpenses) {
            String category = expense.getCategory();
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
        return categories;
    }
    
    /**
     * Calculate total amount for filtered expenses
     */
    public double getTotalAmount() {
        double total = 0;
        for (Expense expense : filteredExpenses) {
            total += expense.getAmount();
        }
        return total;
    }
    
    /**
     * Find highest expense in filtered list
     */
    public double getMaxExpense() {
        if (filteredExpenses.isEmpty()) {
            return 0;
        }
        
        double max = filteredExpenses.get(0).getAmount();
        for (Expense expense : filteredExpenses) {
            if (expense.getAmount() > max) {
                max = expense.getAmount();
            }
        }
        return max;
    }
    
    /**
     * Update the source list and reset filters
     */
    public void updateSourceList(List<Expense> newAllExpenses) {
        this.allExpenses = newAllExpenses;
        this.filteredExpenses = new ArrayList<>(newAllExpenses);
    }
    
    /**
     * Get current filtered list
     */
    public List<Expense> getFilteredExpenses() {
        return filteredExpenses;
    }
    
    /**
     * Get source list
     */
    public List<Expense> getAllExpenses() {
        return allExpenses;
    }
}
