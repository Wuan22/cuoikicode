package com.minimart.model;

import com.minimart.exception.InvalidProductException;

/**
 * Lớp trừu tượng (Abstract class) đại diện cho sản phẩm trong siêu thị.
 * - Đóng gói: thuộc tính private, truy cập qua getter/setter có kiểm tra hợp lệ.
 * - Kế thừa: FoodProduct, ElectronicProduct, HouseholdProduct kế thừa lớp này.
 * - Đa hình: getCategory(), getExtraInfo(), getDiscountRate() được lớp con ghi đè.
 */
public abstract class Product implements Discountable, Comparable<Product> {
    private String id;
    private String name;
    private double price;
    private int quantity;

    protected Product(String id, String name, double price, int quantity) {
        setId(id);
        setName(name);
        setPrice(price);
        setQuantity(quantity);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidProductException("Mã sản phẩm không được để trống.");
        }
        this.id = id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Tên sản phẩm không được để trống.");
        }
        this.name = name.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new InvalidProductException("Giá sản phẩm không được âm.");
        }
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new InvalidProductException("Số lượng tồn kho không được âm.");
        }
        this.quantity = quantity;
    }

    /** Mặc định không giảm giá; lớp con có thể ghi đè. */
    @Override
    public double getDiscountRate() {
        return 0;
    }

    /** Giá bán thực tế sau giảm giá. */
    public double getSellingPrice() {
        return price * (1 - getDiscountRate());
    }

    /** Tên nhóm hàng (đa hình). */
    public abstract String getCategory();

    /** Thông tin riêng của từng loại sản phẩm (đa hình). */
    public abstract String getExtraInfo();

    /** Chuỗi CSV để lưu file: LOẠI;mã;tên;giá;số lượng;thông tin riêng. */
    public abstract String toCsv();

    protected String csvPrefix(String type) {
        return String.join(";", type, id, name.replace(';', ','),
                String.valueOf((long) price), String.valueOf(quantity));
    }

    @Override
    public int compareTo(Product other) {
        return this.id.compareToIgnoreCase(other.id);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
