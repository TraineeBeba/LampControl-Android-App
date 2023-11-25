package com.example.myapplication.fragment;

public class Cat {
    private String name;
    private String additionalInfo; // This could be age, breed, etc.

    public Cat(String name, String additionalInfo) {
        this.name = name;
        this.additionalInfo = additionalInfo;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    // Setters (if needed)
    // ...
}
