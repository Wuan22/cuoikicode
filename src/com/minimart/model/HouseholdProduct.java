package com.minimart.model;

import com.minimart.exception.InvalidProductException;

/** Đồ gia dụng: có thương hiệu, không giảm giá. */
public class HouseholdProduct extends Product {
    private String brand;

    public HouseholdProduct(String id, String name, double price, int quantity, String brand) {
        super(id, name, price, quantity);
        setBrand(brand);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new InvalidProductException("Thương hiệu không được để trống.");
        }
        this.brand = brand.trim();
    }

    @Override
    public String getCategory() {
        return "Gia dụng";
    }

    @Override
    public String getExtraInfo() {
        return "Thương hiệu: " + brand;
    }

    @Override
    public String toCsv() {
        return csvPrefix("HOME") + ";" + brand.replace(';', ',');
    }
}
