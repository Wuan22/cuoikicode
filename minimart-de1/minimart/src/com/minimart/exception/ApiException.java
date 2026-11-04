package com.minimart.exception;

/** Ngoại lệ checked: lỗi khi gọi API bên ngoài (mất mạng, timeout, dữ liệu sai định dạng). */
public class ApiException extends Exception {
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(String message) {
        super(message);
    }
}
