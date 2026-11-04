package com.minimart.ui;

import com.minimart.exception.InvalidProductException;
import com.minimart.model.ElectronicProduct;
import com.minimart.model.FoodProduct;
import com.minimart.model.HouseholdProduct;
import com.minimart.model.Product;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Hộp thoại thêm / sửa sản phẩm. Trả về null nếu người dùng hủy. */
public final class ProductDialog {
    private static final String[] TYPES = {"Thực phẩm", "Điện tử", "Gia dụng"};
    private static final String[] EXTRA_LABELS = {
            "Hạn sử dụng (yyyy-MM-dd):", "Bảo hành (tháng):", "Thương hiệu:"};

    private ProductDialog() {
    }

    public static Product show(Component parent, Product existing) {
        JComboBox<String> type = new JComboBox<>(TYPES);
        JTextField id = Theme.field(null, 15);
        JTextField name = Theme.field(null, 15);
        JTextField price = Theme.field(null, 15);
        JTextField qty = Theme.field(null, 15);
        JTextField extra = Theme.field(null, 15);
        JLabel extraLabel = new JLabel(EXTRA_LABELS[0]);

        type.addActionListener(e -> extraLabel.setText(EXTRA_LABELS[type.getSelectedIndex()]));

        if (existing != null) {
            type.setEnabled(false);
            id.setEditable(false);
            id.setText(existing.getId());
            name.setText(existing.getName());
            price.setText(String.valueOf((long) existing.getPrice()));
            qty.setText(String.valueOf(existing.getQuantity()));
            if (existing instanceof FoodProduct f) {
                type.setSelectedIndex(0);
                extra.setText(f.getExpiryDate().toString());
            } else if (existing instanceof ElectronicProduct el) {
                type.setSelectedIndex(1);
                extra.setText(String.valueOf(el.getWarrantyMonths()));
            } else if (existing instanceof HouseholdProduct h) {
                type.setSelectedIndex(2);
                extra.setText(h.getBrand());
            }
            extraLabel.setText(EXTRA_LABELS[type.getSelectedIndex()]);
        } else {
            extra.setText(LocalDate.now().plusMonths(1).toString());
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Loại sản phẩm:"));
        panel.add(type);
        panel.add(new JLabel("Mã:"));
        panel.add(id);
        panel.add(new JLabel("Tên:"));
        panel.add(name);
        panel.add(new JLabel("Giá (đ):"));
        panel.add(price);
        panel.add(new JLabel("Số lượng:"));
        panel.add(qty);
        panel.add(extraLabel);
        panel.add(extra);

        String title = existing == null ? "Thêm sản phẩm" : "Sửa sản phẩm";
        while (true) {
            int choice = JOptionPane.showConfirmDialog(parent, panel, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                double p = Double.parseDouble(price.getText().trim());
                int q = Integer.parseInt(qty.getText().trim());
                String x = extra.getText().trim();
                return switch (type.getSelectedIndex()) {
                    case 0 -> new FoodProduct(id.getText(), name.getText(), p, q, LocalDate.parse(x));
                    case 1 -> new ElectronicProduct(id.getText(), name.getText(), p, q, Integer.parseInt(x));
                    default -> new HouseholdProduct(id.getText(), name.getText(), p, q, x);
                };
            } catch (NumberFormatException e) {
                error(parent, "Giá, số lượng (và bảo hành) phải là số hợp lệ.");
            } catch (DateTimeParseException e) {
                error(parent, "Hạn sử dụng phải có dạng yyyy-MM-dd, ví dụ 2026-12-31.");
            } catch (InvalidProductException e) {
                error(parent, e.getMessage());
            }
        }
    }

    private static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
    }
}
