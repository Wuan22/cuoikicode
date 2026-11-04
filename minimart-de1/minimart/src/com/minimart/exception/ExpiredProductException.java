package com.minimart.exception;

/** Ngoại lệ checked: sản phẩm đã hết hạn sử dụng, không được bán. */
public class ExpiredProductException extends Exception {
    public ExpiredProductException(String productName) {
        super("Sản phẩm \"" + productName + "\" đã hết hạn sử dụng, không thể bán.");
    }
}
