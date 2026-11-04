package com.minimart.ui;

import com.minimart.service.ProductRepository;
import com.minimart.service.SalesService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Cửa sổ chính: thanh tiêu đề + 3 tab (Sản phẩm, Bán hàng, Hóa đơn). */
public class MainFrame extends JFrame {
    public MainFrame(ProductRepository repo, SalesService sales, Path dataFile) {
        super("Hệ thống quản lý bán hàng và siêu thị mini");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = Theme.tabs();
        tabs.addTab("Sản phẩm", new ProductPanel(repo, dataFile));
        tabs.addTab("Bán hàng", new SalesPanel(repo, sales));
        tabs.addTab("Hóa đơn", new InvoicePanel(sales));
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() instanceof Refreshable r) {
                r.refresh();
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.DARK);
        header.setBorder(new EmptyBorder(11, 16, 11, 16));
        header.add(Theme.label("MiniMart", Theme.TITLE, Color.WHITE), BorderLayout.WEST);
        header.add(Theme.label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                Theme.SMALL, new Color(0x9CA3AF)), BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);

        setSize(1000, 620);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(null);
    }
}
