# Đề tài 1: Hệ thống quản lý bán hàng và siêu thị mini

Ứng dụng Java Swing (JDK 17 trở lên). Sinh viên: Bùi Nguyễn Minh Quân – MSSV 241506121 - Nguyễn Nam Bình - MSSV 24150604 - Hoàng Việt - MSSV 22130007 - Hồ Hưng Chí - MSSV 24150702
Học phần: Lập trình hướng đối tượng – Lớp học phần 020100116801 – HKI 2026-2027

## Chạy chương trình

Windows:
```
build.bat
run.bat
```
Linux/macOS:
```
chmod +x build.sh run.sh && ./build.sh && ./run.sh
```
Hoặc dùng IDE (IntelliJ/Eclipse/NetBeans): mở thư mục `src`, chạy lớp `com.minimart.Main`.

Chạy kiểm thử logic (không cần giao diện):
```
javac -encoding UTF-8 -cp out -d out test/SmokeTest.java
java -cp out SmokeTest
```

## Chức năng

- **Tab Sản phẩm**: xem, tìm kiếm theo mã/tên, thêm, sửa, xóa, lưu/tải file `data/products.csv`.
- **Tab Bán hàng**: chọn sản phẩm → giỏ hàng → thanh toán, tự trừ tồn kho, xuất hóa đơn; hiển thị quy đổi USD theo tỷ giá lấy từ API.
- **Tab Hóa đơn & Doanh thu**: danh sách hóa đơn, xem chi tiết, tổng doanh thu, xuất hóa đơn ra `invoices/HDxxx.txt`.

## Ánh xạ yêu cầu đề bài

### Tam giác P.I.E.
| Nội dung | Vị trí trong mã nguồn |
|---|---|
| Đóng gói (Encapsulation) | Thuộc tính `private` + getter/setter có kiểm tra hợp lệ trong `Product`, `FoodProduct`, `ElectronicProduct`, `HouseholdProduct`; `Invoice` bất biến |
| Kế thừa (Inheritance) | `FoodProduct`, `ElectronicProduct`, `HouseholdProduct` kế thừa `Product` |
| Đa hình (Polymorphism) | Ghi đè `getCategory()`, `getExtraInfo()`, `getDiscountRate()`, `toCsv()`; giao diện và kho chỉ làm việc với kiểu `Product` |
| Abstract class | `Product` (có phương thức abstract) |
| Interface | `Discountable` (do `Product` cài đặt), `Refreshable` (do các panel cài đặt), `Comparable<Product>` |
| Lambda | `stream().filter().sorted()` trong `ProductRepository.search`, `mapToDouble(...).sum()` trong `SalesService`/`Invoice`, `forEach`, `ActionListener` dạng lambda trong toàn bộ giao diện |

### Ngoại lệ và API
- Ngoại lệ tự định nghĩa (package `exception`):
  - Checked: `ProductNotFoundException`, `OutOfStockException`, `ExpiredProductException`, `ApiException`
  - Unchecked: `InvalidProductException` (dữ liệu không hợp lệ, trùng mã)
- Xử lý ngoại lệ: `try/catch` (multi-catch) ở lớp giao diện, hiển thị hộp thoại lỗi thân thiện; `IOException` khi đọc/ghi file.
- API: `ExchangeRateService` dùng `java.net.http.HttpClient` gọi `https://open.er-api.com/v6/latest/USD` để lấy tỷ giá VND/USD, chạy nền bằng `SwingWorker`; lỗi mạng được gói thành `ApiException` và hiển thị trên giao diện (chương trình vẫn dùng bình thường khi offline).

### Lập trình giao diện
- Giao diện phẳng hiện đại tự thiết kế trong `Theme.java` (không cần thư viện ngoài): thanh tiêu đề, tab gạch chân, thẻ bo góc, bảng kẻ sọc, nút bo tròn.
- Java Swing: `JFrame`, `JTabbedPane`, `JTable`, `JSplitPane`, `JSpinner`, `JOptionPane`, `SwingWorker`.
- Bắt sự kiện: `ActionListener`, `DocumentListener` (tìm kiếm tức thời), `ListSelectionListener`, `ChangeListener` (làm mới dữ liệu khi đổi tab).

## Cấu trúc thư mục
```
src/com/minimart/
  Main.java
  model/      Product (abstract), FoodProduct, ElectronicProduct, HouseholdProduct,
              Discountable, CartItem, Invoice
  exception/  5 ngoại lệ tự định nghĩa
  service/    ProductRepository, SalesService, ExchangeRateService
  ui/         Theme, MainFrame, ProductPanel, SalesPanel, InvoicePanel, ProductDialog, Fmt, Refreshable
test/         SmokeTest.java
```

## Quy tắc nghiệp vụ
- Thực phẩm còn ≤ 3 ngày hạn dùng: giảm 30%; hết hạn: không được bán.
- Điện tử: khuyến mãi 5%. Gia dụng: không giảm.
- Không cho thêm vào giỏ vượt quá tồn kho; kiểm tra lại khi thanh toán.
