package com.minimart.ui;

import com.minimart.model.Invoice;
import com.minimart.service.SalesService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Tab hóa đơn và doanh thu. */
public class InvoicePanel extends JPanel implements Refreshable {
    private final SalesService sales;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Thời gian", "Khách hàng", "Tổng tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextArea detail = new JTextArea();
    private final JLabel revenue = Theme.label("0 đ", Theme.BIG, Theme.PRIMARY);
    private final JLabel count = Theme.label("0", Theme.BIG, Theme.TEXT);
    private final JLabel average = Theme.label("0 đ", Theme.BIG, Theme.TEXT);

    public InvoicePanel(SalesService sales) {
        this.sales = sales;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel page = Theme.page();

        JPanel stats = Theme.transparent(new GridLayout(1, 3, 12, 0));
        stats.add(stat("Doanh thu", revenue));
        stats.add(stat("Số hóa đơn", count));
        stats.add(stat("Trung bình / hóa đơn", average));

        Theme.style(table, 3);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetail();
            }
        });
        JPanel listCard = Theme.card(1);
        listCard.add(Theme.scroll(table), BorderLayout.CENTER);

        detail.setEditable(false);
        detail.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detail.setBorder(new EmptyBorder(6, 6, 6, 6));
        JButton export = Theme.button("Xuất file .txt", Theme.Kind.SECONDARY);
        export.addActionListener(e -> onExport());
        JPanel head = Theme.transparent(new BorderLayout());
        head.add(Theme.heading("Chi tiết hóa đơn"), BorderLayout.WEST);
        head.add(export, BorderLayout.EAST);
        JPanel detailCard = Theme.card(12);
        detailCard.add(head, BorderLayout.NORTH);
        detailCard.add(Theme.scroll(detail), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listCard, detailCard);
        split.setResizeWeight(0.4);
        Theme.split(split);

        page.add(stats, BorderLayout.NORTH);
        page.add(split, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
        refresh();
    }

    private static JPanel stat(String title, JLabel value) {
        JPanel c = Theme.card(14);
        c.setLayout(new BorderLayout(0, 4));
        c.add(Theme.label(title, Theme.SMALL, Theme.MUTED), BorderLayout.NORTH);
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        sales.getInvoices().forEach(inv -> model.addRow(new Object[]{
                inv.getId(), inv.getTime().format(f), inv.getCustomerName(), Fmt.money(inv.getTotal())}));
        int n = sales.getInvoices().size();
        double total = sales.getTotalRevenue();
        revenue.setText(Fmt.money(total));
        count.setText(String.valueOf(n));
        average.setText(Fmt.money(n == 0 ? 0 : total / n));
        detail.setText("");
    }

    private Invoice selected() {
        int row = table.getSelectedRow();
        List<Invoice> list = sales.getInvoices();
        return (row >= 0 && row < list.size()) ? list.get(row) : null;
    }

    private void showDetail() {
        Invoice inv = selected();
        detail.setText(inv == null ? "" : inv.toText());
        detail.setCaretPosition(0);
    }

    private void onExport() {
        Invoice inv = selected();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một hóa đơn.");
            return;
        }
        Path out = Path.of("invoices", inv.getId() + ".txt");
        try {
            Files.createDirectories(out.getParent());
            Files.writeString(out, inv.toText(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Đã xuất: " + out.toAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
