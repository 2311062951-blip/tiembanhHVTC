# 🍰 HVTC BAKERY - Website Bán Bánh Online Cao Cấp

HVTC BAKERY là dự án thương mại điện tử chuyên nghiệp bán các dòng bánh tươi thủ công (bánh mì ngàn lớp, bánh ngọt Mousse, Cheesecake, bánh quy bơ và bánh sinh nhật tiệc mừng) được xây dựng hoàn chỉnh bằng **Java 17/21**, **Spring Boot 3.x**, **Spring Data JPA**, **Spring Security** và **Thymeleaf**.

---

## 🚀 Công Nghệ Sử Dụng

- **Backend**: Java 21 / Java 17, Spring Boot 3.2.4, Spring MVC, Spring Data JPA, Spring Security 6, Hibernate, Lombok, Validation.
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript, Bootstrap 5, Font Awesome.
- **Database**: MySQL 8.x (Hỗ trợ fallback H2 cho testing offline).
- **Architecture**: Kiến trúc Layered 3 tầng chuẩn (Controller, Service/Impl, Repository, Entity, DTO).
- **AI Chatbot**: Tích hợp DI Architecture (`ChatbotService`, `AIChatService`) hỗ trợ tư vấn chọn bánh, tra cứu giá & hàng trong kho.

---

## 🔐 Tài Khoản Mẫu Trực Tiếp Để Test Hệ Thống

| Nhóm Quyền | Tên Đăng Nhập (Username) | Mật Khẩu (Password) | Quyền Hạn (Role) |
| :--- | :--- | :--- | :--- |
| **Quản Trị Viên (Admin)** | `admin` | `Admin@123` | `ROLE_ADMIN`, `ROLE_USER` |
| **Khách Hàng (User)** | `user` | `User@123` | `ROLE_USER` |

> 📌 *Lưu ý: Mật khẩu được mã hóa an toàn bằng thuật toán BCrypt Hash trong Database.*

---

## 🛠️ Hướng Dẫn Mở & Chạy Project Bằng IntelliJ IDEA

### 1. Chuẩn Bị Môi Trường
- **JDK**: Java 17 trở lên (đã test hoàn hảo trên Java 21).
- **IntelliJ IDEA**: Bản Community hoặc Ultimate.
- **MySQL Server 8.0+** (Ví dụ qua XAMPP, MySQL Workbench hoặc Docker).

### 2. Cấu Hình Database MySQL
1. Mở MySQL Client / Workbench hoặc phpMyAdmin và tạo database:
   ```sql
   CREATE DATABASE hvtc_bakery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Mở file `src/main/resources/application.properties` và chỉnh sửa thông tin tài khoản MySQL của bạn:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hvtc_bakery?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
   spring.datasource.username=root
   spring.datasource.password=root
   ```

### 3. Mở Project Trong IntelliJ IDEA
1. Mở IntelliJ IDEA ➔ **File** ➔ **Open** ➔ Thư mục `HVTC-Bakery` (hoặc chọn file `pom.xml`).
2. Khởi động lại Maven Sync: Nhấp chuột phải vào `pom.xml` ➔ **Maven** ➔ **Reload project**.
3. IntelliJ IDEA sẽ tự động tải toàn bộ thư viện dependencies từ Maven Central.

### 4. Chạy Ứng Dụng
- **Cách 1 (Từ IntelliJ)**: Tìm file `src/main/java/com/hvtc/bakery/HvtcBakeryApplication.java`, nhấp chuột phải chọn **Run 'HvtcBakeryApplication'**.
- **Cách 2 (Từ Terminal)**:
  ```bash
  mvn spring-boot:run
  ```

---

## 🌐 Truy Cập Website

Sau khi ứng dụng khởi chạy thành công (`Started HvtcBakeryApplication in ... seconds`):
- **Trang chủ Khách hàng**: [http://localhost:8080](http://localhost:8080)
- **Trang Quản trị Admin**: [http://localhost:8080/admin](http://localhost:8080/admin) *(Cần đăng nhập bằng tài khoản `admin / Admin@123`)*

---

## 🧪 Chạy Kiểm Thử (Unit & Integration Tests)

Dự án bao gồm bộ Unit Test đầy đủ cho tầng Service:
```bash
mvn test
```
Các test bao gồm:
- `ProductServiceTest`: Kiểm tra lọc bánh, thêm bánh mới và lấy giá hiệu lực.
- `UserServiceTest`: Kiểm tra đăng ký tài khoản, mã hóa BCrypt và kiểm tra trùng lặp.
- `CartServiceTest`: Kiểm tra tính năng giỏ hàng, cập nhật số lượng và kiểm tra tồn kho.
- `OrderServiceTest`: Kiểm tra quy trình đặt hàng, trừ tồn kho và tính phí vận chuyển miễn phí.

---

## 📁 Cấu Trúc Project

```
HVTC-Bakery/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/hvtc/bakery/
    │   │   ├── config/             # Spring Security, WebMvc, DataInitializer
    │   │   ├── controller/         # Web MVC Controllers (Product, Cart, Checkout, Admin, Auth...)
    │   │   ├── api/                # REST API Controllers (Product, Category, Chatbot...)
    │   │   ├── dto/                # Data Transfer Objects
    │   │   ├── entity/             # JPA Entities (Product, Order, User, Cart, Category...)
    │   │   ├── repository/         # Spring Data JPA Repositories
    │   │   ├── security/           # CustomUserDetails & CustomUserDetailsService
    │   │   ├── service/            # Business Interfaces & Service Implementations
    │   │   ├── exception/          # Global Exception Handler & Custom Exceptions
    │   │   └── util/               # SlugUtils & CodeGenerator
    │   └── resources/
    │       ├── application.properties
    │       ├── schema.sql          # MySQL Schema Definition
    │       ├── static/             # CSS, JS, Images, Uploads
    │       └── templates/          # Thymeleaf HTML Views (Home, Product, Cart, Admin...)
    └── test/java/com/hvtc/bakery/  # JUnit 5 & Mockito Unit Tests
```

---

## 🤝 Git & Tiếp Tục Phát Triển

Project đã được cấu hình `.gitignore` chuẩn cho Java Spring Boot & IntelliJ IDEA. Bạn có thể tiến hành commit và push trực tiếp lên GitHub:

```bash
git init
git add .
git commit -m "Initial commit - Full HVTC Bakery E-commerce Project"
git remote add origin <URL_REPOSITORY_CỦA_BẠN>
git push -u origin main
```
