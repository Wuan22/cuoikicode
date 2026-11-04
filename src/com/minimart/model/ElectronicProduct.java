package com.minimart.model;

import com.minimart.exception.InvalidProductException;

/** Đồ điện tử: có thời gian bảo hành, được khuyến mãi cố định 5%. */
public class ElectronicProduct extends Product {
    private int warrantyMonths;

    public ElectronicProduct(String id, String name, double price, int quantity, int warrantyMonths) {
        super(id, name, price, quantity);
        setWarrantyMonths(warrantyMonths);
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        if (warrantyMonths < 0) {
            throw new InvalidProductException("Thời gian bảo hành không được âm.");
        }
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public double getDiscountRate() {
        return 0.05;
    }

    @Override
    public String getCategory() {
        return "Điện tử";
    }

    @Override
    public String getExtraInfo() {
        return "Bảo hành: " + warrantyMonths + " tháng";
    }

    @Override
    public String toCsv() {
        return csvPrefix("ELEC") + ";" + warrantyMonths;
    }
}
