package com.example.project;

import java.util.Arrays;
import java.util.List;

public class Constants {
    
    // Expense categories
    public static final List<String> EXPENSE_CATEGORIES = Arrays.asList(
        "Potraviny", "Bývanie", "Doprava", "Zábava",
        "Oblečenie", "Zdravie", "Jedlo", "Iné"
    );
    
    // Category colors
    public static final String CATEGORY_COLOR_POTRAVINY = "#4CAF50";
    public static final String CATEGORY_COLOR_BYVANIE = "#2196F3";
    public static final String CATEGORY_COLOR_DOPRAVA = "#FF9800";
    public static final String CATEGORY_COLOR_ZABAVA = "#9C27B0";
    public static final String CATEGORY_COLOR_OBLECENIE = "#E91E63";
    public static final String CATEGORY_COLOR_ZDRAVIE = "#F44336";
    public static final String CATEGORY_COLOR_JEDLO = "#795548";
    public static final String CATEGORY_COLOR_INE = "#607D8B";
    
    // Chart colors
    public static final String CHART_COLOR_PRIMARY = "#FF5722";
    public static final String CHART_COLOR_BACKGROUND = "#FFFFFF";
    public static final String CHART_COLOR_GRID = "#E0E0E0";
    
    // Filter types
    public static final String FILTER_DAILY = "Deň";
    public static final String FILTER_MONTHLY = "Mesiac";
    public static final String FILTER_YEARLY = "Rok";
    
    // Time periods
    public static final int DAYS_TO_SHOW = 7;
    public static final int MONTHS_TO_SHOW = 12;
    public static final int YEARS_TO_SHOW = 5;
    
    // UI constants
    public static final int RECENT_EXPENSES_LIMIT = 3;
    public static final int PROFILE_DRAWER_WIDTH = 800;
    public static final int ANIMATION_DURATION = 300;
    
    // Request codes
    public static final int REQUEST_CODE_ADD_EXPENSE = 1;
    public static final int REQUEST_CODE_EDIT_EXPENSE = 2;
    public static final int REQUEST_CODE_CATEGORY_MANAGEMENT = 3;
}
