package com.minimart.exception;

/** Ngoại lệ unchecked: dữ liệu sản phẩm không hợp lệ (giá âm, mã rỗng, trùng mã...). */
public class InvalidProductException extends RuntimeException {
    public InvalidProductException(String message) {
        super(message);
    }
}
