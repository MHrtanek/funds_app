package com.example.project;
import java.io.Serializable;
import java.util.Date;

public class Expense implements Serializable {
    private String id;
    private double amount;
    private String description;
    private String category;
    private Date date;

    public Expense(double amount, String description, String category, Date date) {
        this.id = java.util.UUID.randomUUID().toString();
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }

    // Gettery a settery
    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
    
    // Settery pre editovanie
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(Date date) { this.date = date; }


}
