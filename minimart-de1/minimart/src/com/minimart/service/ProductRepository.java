package com.minimart.service;

import com.minimart.exception.InvalidProductException;
import com.minimart.exception.ProductNotFoundException;
import com.minimart.model.ElectronicProduct;
import com.minimart.model.FoodProduct;
import com.minimart.model.HouseholdProduct;
import com.minimart.model.Product;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Kho sản phẩm: thêm/sửa/xóa/tìm kiếm và lưu/đọc file CSV. */
public class ProductRepository {
    private final Map<String, Product> products = new LinkedHashMap<>();

    public void add(Product p) {
        if (products.containsKey(p.getId())) {
            throw new InvalidProductException("Mã sản phẩm đã tồn tại: " + p.getId());
        }
        products.put(p.getId(), p);
    }

    public void update(Product p) throws ProductNotFoundException {
        if (!products.containsKey(p.getId())) {
            throw new ProductNotFoundException(p.getId());
        }
        products.put(p.getId(), p);
    }

    public Product findById(String id) throws ProductNotFoundException {
        Product p = products.get(id);
        if (p == null) {
            throw new ProductNotFoundException(id);
        }
        return p;
    }

    public void remove(String id) throws ProductNotFoundException {
        if (products.remove(id) == null) {
            throw new ProductNotFoundException(id);
        }
    }

    public List<Product> getAll() {
        return new ArrayList<>(products.values());
    }

    /** Tìm theo mã hoặc tên (không phân biệt hoa thường), kết quả sắp theo mã. */
    public List<Product> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return products.values().stream()
                .filter(p -> p.getId().toLowerCase().contains(k) || p.getName().toLowerCase().contains(k))
                .sorted()
                .collect(Collectors.toList());
    }

    public void saveToFile(Path path) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        List<String> lines = products.values().stream().map(Product::toCsv).collect(Collectors.toList());
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public void loadFromFile(Path path) throws IOException {
        Map<String, Product> loaded = new LinkedHashMap<>();
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            try {
                Product p = fromCsv(line);
                loaded.put(p.getId(), p);
            } catch (RuntimeException e) {
                throw new IOException("Dòng dữ liệu không hợp lệ: " + line, e);
            }
        }
        products.clear();
        products.putAll(loaded);
    }

    private static Product fromCsv(String line) {
        String[] f = line.split(";", -1);
        String id = f[1];
        String name = f[2];
        double price = Double.parseDouble(f[3]);
        int qty = Integer.parseInt(f[4]);
        return switch (f[0]) {
            case "FOOD" -> new FoodProduct(id, name, price, qty, LocalDate.parse(f[5]));
            case "ELEC" -> new ElectronicProduct(id, name, price, qty, Integer.parseInt(f[5]));
            case "HOME" -> new HouseholdProduct(id, name, price, qty, f[5]);
            default -> throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + f[0]);
        };
    }

    public void seedSampleData() {
        LocalDate today = LocalDate.now();
        add(new FoodProduct("TP001", "Sữa tươi 1L", 32000, 50, today.plusDays(2)));
        add(new FoodProduct("TP002", "Mì gói thùng 30 gói", 115000, 20, today.plusMonths(6)));
        add(new FoodProduct("TP003", "Bánh mì sandwich", 25000, 15, today.plusDays(10)));
        add(new ElectronicProduct("DT001", "Tai nghe Bluetooth", 450000, 12, 12));
        add(new ElectronicProduct("DT002", "Sạc dự phòng 10000mAh", 320000, 18, 6));
        add(new HouseholdProduct("GD001", "Nồi cơm điện 1.8L", 890000, 8, "Sunhouse"));
        add(new HouseholdProduct("GD002", "Chảo chống dính 26cm", 180000, 25, "Elmich"));
    }
}
