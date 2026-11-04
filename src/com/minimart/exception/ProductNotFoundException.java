package com.minimart.exception;

/** Ngoại lệ checked: không tìm thấy sản phẩm theo mã. */
public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String id) {
        super("Không tìm thấy sản phẩm có mã: " + id);
    }
}
