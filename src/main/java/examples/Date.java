package examples;

import java.time.LocalDate;

public class Date {
    private int year;
    private int month;
    private int day;
    LocalDate date;
    public Date(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        date = LocalDate.of(year, month, day);
    }

    public Date() {
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}