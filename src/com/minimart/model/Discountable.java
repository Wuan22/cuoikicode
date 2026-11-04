package com.minimart.model;

/** Interface: đối tượng có thể được giảm giá. */
public interface Discountable {
    /** Tỷ lệ giảm giá từ 0.0 đến 1.0. */
    double getDiscountRate();
}
