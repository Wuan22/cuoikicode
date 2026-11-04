package com.minimart.model;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Hóa đơn bán hàng (bất biến sau khi tạo). */
public class Invoice {
    private final String id;
    private final LocalDateTime time;
    private final String customerName;
    private final List<CartItem> items;

    public Invoice(String id, String customerName, List<CartItem> items) {
        this.id = id;
        this.time = LocalDateTime.now();
        this.customerName = customerName;
        this.items = List.copyOf(items);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getTotal).sum();
    }

    public String toText() {
        DecimalFormat df = new DecimalFormat("#,##0");
        StringBuilder sb = new StringBuilder();
        sb.append("========== HÓA ĐƠN ").append(id).append(" ==========\n");
        sb.append("Thời gian : ").append(time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append('\n');
        sb.append("Khách hàng: ").append(customerName).append("\n\n");
        for (CartItem it : items) {
            sb.append(String.format("%-28s %3d x %12s = %14s%n",
                    it.getProduct().getName(), it.getQuantity(),
                    df.format(it.getUnitPrice()), df.format(it.getTotal())));
        }
        sb.append("\nTỔNG CỘNG: ").append(df.format(getTotal())).append(" đ\n");
        return sb.toString();
    }
}
