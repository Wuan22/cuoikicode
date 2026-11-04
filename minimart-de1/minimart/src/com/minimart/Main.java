package com.minimart;

import com.minimart.service.ProductRepository;
import com.minimart.service.SalesService;
import com.minimart.ui.MainFrame;
import com.minimart.ui.Theme;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path dataFile = Path.of("data", "products.csv");
        ProductRepository repo = new ProductRepository();
        try {
            if (Files.exists(dataFile)) {
                repo.loadFromFile(dataFile);
            } else {
                repo.seedSampleData();
            }
        } catch (IOException e) {
            System.err.println("Không đọc được dữ liệu, dùng dữ liệu mẫu: " + e.getMessage());
            repo.seedSampleData();
        }
        SalesService sales = new SalesService(repo);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // dùng giao diện mặc định nếu không đặt được
            }
            Theme.install();
            new MainFrame(repo, sales, dataFile).setVisible(true);
        });
    }
}
