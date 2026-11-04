package com.minimart.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Thực phẩm: có hạn sử dụng, giảm 30% khi còn không quá 3 ngày. */
public class FoodProduct extends Product {
    private LocalDate expiryDate;

    public FoodProduct(String id, String name, double price, int quantity, LocalDate expiryDate) {
        super(id, name, price, quantity);
        setExpiryDate(expiryDate);
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new com.minimart.exception.InvalidProductException("Hạn sử dụng không được để trống.");
        }
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    @Override
    public double getDiscountRate() {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return (days >= 0 && days <= 3) ? 0.30 : 0;
    }

    @Override
    public String getCategory() {
        return "Thực phẩm";
    }

    @Override
    public String getExtraInfo() {
        return "HSD: " + expiryDate + (isExpired() ? " (HẾT HẠN)" : "");
    }

    @Override
    public String toCsv() {
        return csvPrefix("FOOD") + ";" + expiryDate;
    }
}
