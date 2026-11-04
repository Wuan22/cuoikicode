import com.minimart.exception.*;
import com.minimart.model.*;
import com.minimart.service.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class SmokeTest {
    static int failed = 0;

    static void check(String name, boolean cond) {
        System.out.println((cond ? "[OK]   " : "[FAIL] ") + name);
        if (!cond) failed++;
    }

    public static void main(String[] args) throws Exception {
        ProductRepository repo = new ProductRepository();
        repo.seedSampleData();
        SalesService sales = new SalesService(repo);

        check("Seed 7 sản phẩm", repo.getAll().size() == 7);
        check("Đa hình: thực phẩm sắp hết hạn giảm 30%", repo.findById("TP001").getDiscountRate() == 0.30);
        check("Đa hình: điện tử giảm 5%", repo.findById("DT001").getDiscountRate() == 0.05);
        check("Đa hình: gia dụng không giảm", repo.findById("GD001").getDiscountRate() == 0);
        check("Tìm kiếm 'sữa'", repo.search("sữa").size() == 1);

        try { repo.add(new HouseholdProduct("GD001", "Trùng", 1, 1, "X")); check("Trùng mã", false); }
        catch (InvalidProductException e) { check("Trùng mã bị chặn", true); }
        try { new HouseholdProduct("A", "B", -5, 1, "X"); check("Giá âm", false); }
        catch (InvalidProductException e) { check("Giá âm bị chặn", true); }
        try { repo.findById("ZZZ"); check("Không tìm thấy", false); }
        catch (ProductNotFoundException e) { check("ProductNotFoundException", true); }

        sales.addToCart("GD001", 3);
        sales.addToCart("GD001", 2);
        check("Gộp dòng giỏ hàng", sales.getCart().size() == 1 && sales.getCart().get(0).getQuantity() == 5);
        try { sales.addToCart("GD001", 100); check("Vượt tồn kho", false); }
        catch (OutOfStockException e) { check("OutOfStockException (còn " + e.getAvailable() + ")", true); }

        repo.add(new FoodProduct("TP999", "Sữa hết hạn", 10000, 5, java.time.LocalDate.now().minusDays(1)));
        try { sales.addToCart("TP999", 1); check("Hết hạn", false); }
        catch (ExpiredProductException e) { check("ExpiredProductException", true); }

        Invoice inv = sales.checkout("");
        check("Hóa đơn tên khách mặc định", inv.getCustomerName().equals("Khách lẻ"));
        check("Trừ kho sau thanh toán", repo.findById("GD001").getQuantity() == 3);
        check("Doanh thu = tổng hóa đơn", sales.getTotalRevenue() == 5 * 890000.0);
        try { sales.checkout("x"); check("Giỏ trống", false); }
        catch (IllegalStateException e) { check("Giỏ trống bị chặn", true); }

        Path tmp = Files.createTempFile("minimart", ".csv");
        repo.saveToFile(tmp);
        ProductRepository repo2 = new ProductRepository();
        repo2.loadFromFile(tmp);
        check("Lưu/đọc CSV giữ nguyên số sản phẩm", repo2.getAll().size() == repo.getAll().size());
        Files.delete(tmp);

        check("Parse JSON tỷ giá", ExchangeRateService.parseVndRate("{\"rates\":{\"USD\":1,\"VND\":26350.5}}") == 26350.5);
        try { ExchangeRateService.parseVndRate("{}"); check("JSON sai", false); }
        catch (ApiException e) { check("ApiException khi JSON thiếu VND", true); }

        System.out.println(failed == 0 ? "\nTẤT CẢ ĐẠT" : "\nCÓ " + failed + " LỖI");
        System.exit(failed == 0 ? 0 : 1);
    }
}
