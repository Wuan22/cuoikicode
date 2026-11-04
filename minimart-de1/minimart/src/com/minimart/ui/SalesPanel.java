package com.minimart.ui;

import com.minimart.exception.ApiException;
import com.minimart.exception.ExpiredProductException;
import com.minimart.exception.InvalidProductException;
import com.minimart.exception.OutOfStockException;
import com.minimart.exception.ProductNotFoundException;
import com.minimart.model.CartItem;
import com.minimart.model.Invoice;
import com.minimart.service.ExchangeRateService;
import com.minimart.service.ProductRepository;
import com.minimart.service.SalesService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

/** Tab bán hàng: chọn sản phẩm -> giỏ hàng -> thanh toán, kèm quy đổi USD qua API. */
public class SalesPanel extends JPanel implements Refreshable {
    private final ProductRepository repo;
    private final SalesService sales;
    private final ExchangeRateService rateService = new ExchangeRateService();

    private final DefaultTableModel productModel = readOnlyModel("Mã", "Tên", "Giá bán", "Tồn");
    private final DefaultTableModel cartModel = readOnlyModel("Tên", "SL", "Đơn giá", "Thành tiền");
    private final JTable productTable = new JTable(productModel);
    private final JTable cartTable = new JTable(cartModel);
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JTextField customer = Theme.field("Tên khách hàng (không bắt buộc)", 14);
    private final JLabel totalLabel = Theme.label("0 đ", Theme.BIG, Theme.TEXT);
    private final JLabel usdLabel = Theme.label(" ", Theme.SMALL, Theme.MUTED);
    private final JLabel rateLabel = Theme.label("Tỷ giá: chưa tải", Theme.SMALL, Theme.MUTED);
    private Double vndPerUsd;

    public SalesPanel(ProductRepository repo, SalesService sales) {
        this.repo = repo;
        this.sales = sales;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel page = Theme.page();
        JPanel grid = Theme.transparent(new GridLayout(1, 2, 12, 0));
        grid.add(buildLeft());
        grid.add(buildRight());
        page.add(grid, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        refresh();
        loadRate();
    }

    private JPanel buildLeft() {
        Theme.style(productTable, 2, 3);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        qtySpinner.setFont(Theme.BASE);
        qtySpinner.setPreferredSize(new Dimension(70, 32));

        JButton addBtn = Theme.button("Thêm vào giỏ  →", Theme.Kind.PRIMARY);
        addBtn.addActionListener(e -> onAddToCart());

        JPanel bottom = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottom.add(Theme.label("Số lượng", Theme.BASE, Theme.MUTED));
        bottom.add(qtySpinner);
        bottom.add(addBtn);

        JPanel card = Theme.card(12);
        card.add(Theme.heading("Sản phẩm"), BorderLayout.NORTH);
        card.add(Theme.scroll(productTable), BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRight() {
        Theme.style(cartTable, 1, 2, 3);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(36);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JButton removeBtn = Theme.button("Bỏ dòng chọn", Theme.Kind.SECONDARY);
        JButton clearBtn = Theme.button("Xóa giỏ", Theme.Kind.SECONDARY);
        JButton rateBtn = Theme.button("↻ Tỷ giá", Theme.Kind.SECONDARY);
        rateBtn.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        JButton payBtn = Theme.button("THANH TOÁN", Theme.Kind.PRIMARY);
        payBtn.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        removeBtn.addActionListener(e -> onRemove());
        clearBtn.addActionListener(e -> {
            sales.clearCart();
            refreshCart();
        });
        rateBtn.addActionListener(e -> loadRate());
        payBtn.addActionListener(e -> onCheckout());

        JPanel cartButtons = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cartButtons.add(removeBtn);
        cartButtons.add(clearBtn);

        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        usdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel amounts = Theme.transparent(new GridLayout(0, 1, 0, 2));
        amounts.add(totalLabel);
        amounts.add(usdLabel);
        JPanel totalRow = Theme.transparent(new BorderLayout());
        totalRow.add(Theme.label("Tổng cộng", Theme.BASE, Theme.MUTED), BorderLayout.WEST);
        totalRow.add(amounts, BorderLayout.EAST);

        JPanel rateRow = Theme.transparent(new BorderLayout());
        rateRow.add(rateLabel, BorderLayout.WEST);
        rateRow.add(rateBtn, BorderLayout.EAST);

        JPanel summary = Theme.transparent(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 0, 0);
        for (JComponent row : new JComponent[]{cartButtons, totalRow, rateRow, customer, payBtn}) {
            summary.add(row, gc);
        }

        JPanel card = Theme.card(12);
        card.add(Theme.heading("Giỏ hàng"), BorderLayout.NORTH);
        card.add(Theme.scroll(cartTable), BorderLayout.CENTER);
        card.add(summary, BorderLayout.SOUTH);
        return card;
    }

    private static DefaultTableModel readOnlyModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    @Override
    public void refresh() {
        productModel.setRowCount(0);
        repo.search("").forEach(p -> productModel.addRow(new Object[]{
                p.getId(), p.getName(), Fmt.money(p.getSellingPrice()), p.getQuantity()}));
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (CartItem ci : sales.getCart()) {
            cartModel.addRow(new Object[]{ci.getProduct().getName(), ci.getQuantity(),
                    Fmt.money(ci.getUnitPrice()), Fmt.money(ci.getTotal())});
        }
        double total = sales.getCartTotal();
        totalLabel.setText(Fmt.money(total));
        if (vndPerUsd != null) {
            usdLabel.setText("≈ " + new DecimalFormat("#,##0.00").format(total / vndPerUsd) + " USD");
        }
    }

    private void onAddToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một sản phẩm.");
            return;
        }
        String id = (String) productModel.getValueAt(row, 0);
        try {
            sales.addToCart(id, (Integer) qtySpinner.getValue());
            refreshCart();
        } catch (ProductNotFoundException | OutOfStockException | ExpiredProductException
                 | InvalidProductException ex) {
            error(ex.getMessage());
        }
    }

    private void onRemove() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            sales.removeFromCart(row);
            refreshCart();
        }
    }

    private void onCheckout() {
        try {
            Invoice inv = sales.checkout(customer.getText());
            customer.setText("");
            refresh();
            JTextArea area = new JTextArea(inv.toText(), 14, 52);
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Thanh toán thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (OutOfStockException | IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    /** Gọi API ở luồng nền (SwingWorker) để không làm treo giao diện. */
    private void loadRate() {
        rateLabel.setText("Tỷ giá: đang tải...");
        new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws ApiException {
                return rateService.fetchVndPerUsd();
            }

            @Override
            protected void done() {
                try {
                    vndPerUsd = get();
                    rateLabel.setText("1 USD = " + new DecimalFormat("#,##0").format(vndPerUsd) + " VND");
                } catch (ExecutionException ex) {
                    vndPerUsd = null;
                    usdLabel.setText(" ");
                    rateLabel.setText("Không tải được tỷ giá (offline?)");
                    rateLabel.setToolTipText(ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                refreshCart();
            }
        }.execute();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
    }
}
