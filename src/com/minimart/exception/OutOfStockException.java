package com.minimart.exception;

/** Ngoại lệ checked: số lượng yêu cầu vượt quá tồn kho. */
public class OutOfStockException extends Exception {
    private final int available;

    public OutOfStockException(String productName, int requested, int available) {
        super("Không đủ hàng cho \"" + productName + "\": cần " + requested + ", chỉ còn " + available);
        this.available = available;
    }

    public int getAvailable() {
        return available;
    }
}
