package com.minimart.model;

/** Một dòng trong giỏ hàng / hóa đơn. Lưu lại giá bán tại thời điểm thêm vào giỏ. */
public class CartItem {
    private final Product product;
    private int quantity;
    private final double unitPrice;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getSellingPrice();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int extra) {
        this.quantity += extra;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotal() {
        return unitPrice * quantity;
    }
}
