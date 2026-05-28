package com.example.target75;

public class Subject {
    private String name;
    private int present;
    private int absent;

    public Subject(String name, int present, int absent) {
        this.name = name;
        this.present = present;
        this.absent = absent;
    }

    public String getName() { return name; }
    public int getPresent() { return present; }
    public int getAbsent() { return absent; }

    // 🔥 Naya data set karne ke liye Setters:
    public void setPresent(int present) { this.present = present; }
    public void setAbsent(int absent) { this.absent = absent; }

    public int getTotal() {
        return present + absent;
    }

    public int getPercentage() {
        int total = getTotal();
        if (total == 0) return 0;
        return (present * 100) / total;
    }
}