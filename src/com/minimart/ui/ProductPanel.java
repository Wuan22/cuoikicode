package com.minimart.ui;

import com.minimart.exception.InvalidProductException;
import com.minimart.exception.ProductNotFoundException;
import com.minimart.model.Product;
import com.minimart.service.ProductRepository;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Tab quản lý sản phẩm: xem, tìm kiếm, thêm, sửa, xóa, lưu/tải file. */
public class ProductPanel extends JPanel implements Refreshable {
    private final ProductRepository repo;
    private final Path dataFile;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã", "Tên sản phẩm", "Loại", "Giá gốc", "Giảm", "Giá bán", "Tồn kho", "Thông tin"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = Theme.field("Tìm theo mã hoặc tên...", 22);
    private final JLabel count = Theme.label(" ", Theme.SMALL, Theme.MUTED);

    public ProductPanel(ProductRepository repo, Path dataFile) {
        this.repo = repo;
        this.dataFile = dataFile;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildContent() {
        JPanel page = Theme.page();

        JButton add = Theme.button("+ Thêm", Theme.Kind.PRIMARY);
        JButton edit = Theme.button("Sửa", Theme.Kind.SECONDARY);
        JButton delete = Theme.button("Xóa", Theme.Kind.DANGER);
        JButton save = Theme.button("Lưu file", Theme.Kind.SECONDARY);
        JButton load = Theme.button("Tải file", Theme.Kind.SECONDARY);
        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        delete.addActionListener(e -> onDelete());
        save.addActionListener(e -> onSave());
        load.addActionListener(e -> onLoad());

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        JPanel buttons = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (JButton b : new JButton[]{add, edit, delete, save, load}) {
            buttons.add(b);
        }
        JPanel toolbar = Theme.transparent(new BorderLayout(12, 0));
        toolbar.add(search, BorderLayout.WEST);
        toolbar.add(buttons, BorderLayout.EAST);

        Theme.style(table, 3, 4, 5, 6);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(190);

        JPanel card = Theme.card(1);
        card.add(Theme.scroll(table), BorderLayout.CENTER);

        page.add(toolbar, BorderLayout.NORTH);
        page.add(card, BorderLayout.CENTER);
        page.add(count, BorderLayout.SOUTH);
        return page;
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        List<Product> list = repo.search(search.getText());
        list.forEach(p -> model.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory(), Fmt.money(p.getPrice()),
                p.getDiscountRate() > 0 ? "-" + (int) Math.round(p.getDiscountRate() * 100) + "%" : "",
                Fmt.money(p.getSellingPrice()), p.getQuantity(), p.getExtraInfo()}));
        count.setText(list.size() + " sản phẩm");
    }

    private String selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một sản phẩm trong bảng.");
            return null;
        }
        return (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void onAdd() {
        Product p = ProductDialog.show(this, null);
        if (p == null) {
            return;
        }
        try {
            repo.add(p);
            refresh();
        } catch (InvalidProductException ex) {
            showError(ex.getMessage());
        }
    }

    private void onEdit() {
        String id = selectedId();
        if (id == null) {
            return;
        }
        try {
            Product updated = ProductDialog.show(this, repo.findById(id));
            if (updated != null) {
                repo.update(updated);
                refresh();
            }
        } catch (ProductNotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void onDelete() {
        String id = selectedId();
        if (id == null) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm " + id + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                repo.remove(id);
                refresh();
            } catch (ProductNotFoundException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void onSave() {
        try {
            repo.saveToFile(dataFile);
            JOptionPane.showMessageDialog(this, "Đã lưu vào " + dataFile.toAbsolutePath());
        } catch (IOException ex) {
            showError("Không lưu được file: " + ex.getMessage());
        }
    }

    private void onLoad() {
        try {
            repo.loadFromFile(dataFile);
            refresh();
        } catch (IOException ex) {
            showError("Không đọc được file: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
