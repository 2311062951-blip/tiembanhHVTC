package com.hvtc.bakery.config;

import com.hvtc.bakery.entity.*;
import com.hvtc.bakery.repository.*;
import com.hvtc.bakery.util.CodeGenerator;
import com.hvtc.bakery.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final WorkshopRepository workshopRepository;
    private final BlogPostRepository blogPostRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Khởi tạo dữ liệu mẫu cho HVTC BAKERY...");

        // 1. Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        // 2. Admin Account (Username: admin, Password: Admin@123)
        if (!userRepository.existsByUsername("admin")) {
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("Quản Trị Viên HVTC")
                    .email("admin@hvtcbakery.vn")
                    .phone("0988888888")
                    .address("Phố Lê Văn Hiến, Đức Thắng, Bắc Từ Liêm, Hà Nội")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(admin);
            log.info("Đã tạo tài khoản Admin mẫu thành công: admin / Admin@123");
        }

        // 3. Normal User Account (Username: user, Password: User@123)
        if (!userRepository.existsByUsername("user")) {
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);

            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("Nguyễn Văn An")
                    .email("user@gmail.com")
                    .phone("0912345678")
                    .address("123 Phố Cổ, Hoàn Kiếm, Hà Nội")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(user);
            log.info("Đã tạo tài khoản User mẫu thành công: user / User@123");
        }

        // 4. Categories
        if (categoryRepository.count() == 0) {
            seedCategoriesAndProducts();
        }

        // 5. Workshops
        if (workshopRepository.count() == 0) {
            seedWorkshops();
        }

        // 6. Blog Posts
        if (blogPostRepository.count() == 0) {
            seedBlogPosts();
        }

        log.info("Hoàn tất khởi tạo dữ liệu mẫu cho HVTC BAKERY!");
    }

    private void seedCategoriesAndProducts() {
        Category c1 = categoryRepository.save(Category.builder().name("Bánh mì").slug("banh-mi").description("Các loại bánh mì tươi thơm nức, nướng mới mỗi sáng").image("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600").status(true).build());
        Category c2 = categoryRepository.save(Category.builder().name("Bánh ngọt").slug("banh-ngot").description("Bánh ngọt Pháp, Mousse thanh nhẹ và Cheesecake béo ngậy").image("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600").status(true).build());
        Category c3 = categoryRepository.save(Category.builder().name("Bánh quy").slug("banh-quy").description("Bánh quy bơ giòn rụm, bánh quy hạt ngũ cơ cao cấp").image("https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600").status(true).build());
        Category c4 = categoryRepository.save(Category.builder().name("Socola").slug("socola").description("Socola tươi handmade và truffles đắng ngọt đậm đà").image("https://images.unsplash.com/photo-1511381939415-e44015466834?w=600").status(true).build());
        Category c5 = categoryRepository.save(Category.builder().name("Đồ uống").slug("do-uong").description("Trà hoa quả tươi, cà phê pha máy và trà sữa thơm ngậy").image("https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600").status(true).build());
        Category c6 = categoryRepository.save(Category.builder().name("Set bánh").slug("set-banh").description("Hộp quà bánh tươi sang trọng dành tặng đối tác và gia đình").image("https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=600&auto=format&fit=crop").status(true).build());
        Category c7 = categoryRepository.save(Category.builder().name("Bánh sinh nhật").slug("banh-sinh-nhat").description("Bánh kem tiệc sinh nhật thiết kế riêng, mẫu mã tinh tế").image("https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600&auto=format&fit=crop").status(true).build());

        // Products (24 items)
        createProduct("Bánh Mousse Matcha", "banh-mousse-matcha", "Mousse trà xanh Uji Nhật Bản kết hợp cốt bánh gato mịn mượt, vị ngọt dịu nhẹ đắng nhẹ đầu lưỡi.", "Mousse trà xanh Nhật Bản thanh mát ít ngọt", new BigDecimal("280000"), new BigDecimal("250000"), 25, "https://images.unsplash.com/photo-1546039907-7fa05f864c02?w=600&auto=format&fit=crop", c2, true, true);
        createProduct("Bánh Tiramisu", "banh-tiramisu", "Bánh Tiramisu phong cách Ý truyền thống với cốt bánh Savoiardi thấm đẫm cà phê Espresso và rượu Kahlua.", "Tiramisu chuẩn vị Ý béo ngậy thơm nức cà phê", new BigDecimal("320000"), new BigDecimal("290000"), 30, "https://images.unsplash.com/photo-1571115177098-24ec42ed204d?w=600&auto=format&fit=crop", c2, true, true);
        createProduct("Bánh Red Velvet", "banh-red-velvet", "Bánh nhung đỏ kiêu kỳ phủ kem phô mai Cream Cheese chua ngọt nhẹ nhàng quyến rũ.", "Bánh nhung đỏ nồng nàn thơm phức kem phô mai", new BigDecimal("350000"), null, 20, "https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=600&auto=format&fit=crop", c2, true, false);
        createProduct("Bánh Chocolate Truffle", "banh-chocolate-truffle", "Cốt bánh socola nguyên chất 70% cacao đến từ Bỉ, phủ ganache socola mượt mà béo đậm.", "Socola Bỉ đắng dịu hòa quyện lớp kem béo ngậy", new BigDecimal("360000"), new BigDecimal("320000"), 18, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600", c4, true, true);
        createProduct("Bánh Croissant Bơ Pháp", "banh-croissant-bo-phap", "Bánh sừng bò nướng ngàn lớp với bơ Elle & Vire thơm lừng, ngàn lớp giòn tan xốp mịn.", "Croissant ngàn lớp giòn thơm bơ Pháp tươi", new BigDecimal("45000"), new BigDecimal("39000"), 50, "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600", c1, false, true);
        createProduct("Bánh Cookie Bơ Hạnh Nhân", "banh-cookie-bo-hanh-nhan", "Bánh quy bơ giòn rụm kết hợp hạt hạnh nhân nướng thơm phức, lựa chọn lý tưởng dùng cùng trà chiều.", "Cookie bơ giòn ngậy rắc hạnh nhân thơm lừng", new BigDecimal("85000"), null, 40, "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=600&auto=format&fit=crop", c3, false, false);
        createProduct("Bánh Bông Lan Trứng Muối", "banh-bong-lan-trung-muoi", "Cốt bánh bông lan xốp mềm, sốt phô mai hoàng kim béo ngậy cùng trứng muối chín tới thơm bùi và chà bông thượng hạng.", "Bông lan trứng muối đượm vị mặn ngọt hài hòa", new BigDecimal("180000"), new BigDecimal("160000"), 35, "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=600&auto=format&fit=crop", c2, true, true);
        createProduct("Bánh Sinh Nhật Dâu Tây Kem Tươi", "banh-sinh-nhat-dau-tay", "Bánh sinh nhật kem tươi hái tận vườn Đà Lạt, trang trí dâu tươi mọng ngọt lịm tim.", "Bánh sinh nhật dâu tây tươi kem Hokkaido béo mịn", new BigDecimal("420000"), new BigDecimal("390000"), 15, "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600&auto=format&fit=crop", c7, true, true);
        createProduct("Bánh Kem Socola Trái Cây", "banh-kem-socola-trai-cay", "Bánh sinh nhật cốt vani Socola kết hợp việt quất và ki-wi tươi tạo hương vị thanh lịch.", "Bánh tiệc sinh nhật sang trọng vị socola quả mọng", new BigDecimal("450000"), null, 12, "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600", c7, true, false);
        createProduct("Bánh Cheesecake Việt Quất", "banh-cheesecake-viet-quat", "Phô mai tươi nướng chậm hòa quyện mứt việt quất dại chua ngọt mượt mát lạnh.", "New York Cheesecake việt quất dại ngậy béo", new BigDecimal("310000"), new BigDecimal("280000"), 22, "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=600", c2, true, true);
        createProduct("Hộp Bánh Macaron Pháp (6 cái)", "banh-macaron-phap", "Hộp 6 bánh Macaron phong cách Pháp đủ sắc màu vị chanh leo, pistacchio, rasberry, matcha, chocolate, vani.", "Bánh Macaron kiêu kỳ rực rỡ chuẩn vị Paris", new BigDecimal("190000"), null, 25, "https://images.unsplash.com/photo-1569864358642-9d1684040f43?w=600", c3, true, true);
        createProduct("Bánh Mì Garlic Butter (Bơ Tỏi)", "banh-mi-garlic-butter", "Bánh mì bơ tỏi Hàn Quốc nhân kem phô mai thơm phảng phất hương tỏi nướng quyến rũ.", "Bánh mì bơ tỏi phô mai Hàn Quốc sốt béo ngậy", new BigDecimal("55000"), new BigDecimal("48000"), 45, "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=600", c1, false, true);
        createProduct("Bánh Mì Pain au Chocolat", "banh-mi-pain-au-chocolat", "Bánh mì cuộn socola đắng ngàn lớp truyền thống Pháp giòn rụm tan trong miệng.", "Pain au Chocolat nướng mới thơm nức thanh socola", new BigDecimal("48000"), null, 40, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600", c1, false, false);
        createProduct("Socola Tươi Nama Chocolate", "socola-tuoi-nama", "Socola tươi phủ bột cacao mỏng mềm tan chảy tức thì khi đưa vào miệng.", "Nama Chocolate nguyên chất mềm mịn cực đỉnh", new BigDecimal("220000"), new BigDecimal("195000"), 30, "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600", c4, true, false);
        createProduct("Set Quà Bánh Petit HVTC", "set-qua-banh-petit-hvtc", "Set 9 chiếc bánh ngọt mini xinh xắn bao gồm Tart trái cây, Choux vani và Opera cake.", "Set bánh tiệc trà chiều cao cấp thích hợp làm quà", new BigDecimal("380000"), new BigDecimal("350000"), 10, "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=600&auto=format&fit=crop", c6, true, true);
        createProduct("Trà Matcha Latte Kem Béo", "tra-matcha-latte", "Matcha Nhật Bản pha sữa tươi nguyên chất lớp kem béo mặn đượm đà thanh mát.", "Đồ uống Matcha thượng hạng dùng kèm bánh ngọt", new BigDecimal("52000"), null, 60, "https://images.unsplash.com/photo-1515823662972-da6a2e4d3002?w=600&auto=format&fit=crop", c5, false, false);
        createProduct("Cà Phê Cold Brew Cam Sả", "ca-phe-cold-brew-cam-sa", "Cà phê ủ lạnh 18 tiếng ngâm tinh dầu cam vàng và sả tươi thanh lọc sảng khoái.", "Cà phê Cold Brew vị trái cây tươi sáng độc đáo", new BigDecimal("58000"), new BigDecimal("49000"), 50, "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600", c5, false, true);
        createProduct("Bánh Choux Vani Hoàng Gia", "banh-choux-vani", "Bánh su kem nhân kem trứng vani Madagascar béo thơm căng tròn mát lạnh.", "Bánh su kem vỏ giòn nhân vani thơm phức", new BigDecimal("65000"), null, 40, "https://images.unsplash.com/photo-1550617931-e17a7b70dce2?w=600&auto=format&fit=crop", c2, false, false);
        createProduct("Bánh Tart Trái Cây Mùa Hè", "banh-tart-trai-cay", "Vỏ bánh tart bơ giòn ruộm chứa kem custard nhẹ và xếp tràn đầy quả mọng.", "Tart trái cây cầu vồng rực rỡ tươi mát", new BigDecimal("75000"), new BigDecimal("65000"), 35, "https://images.unsplash.com/photo-1519869325930-281384150729?w=600", c2, false, true);
        createProduct("Bánh Sinh Nhật Socola Vani Classic", "banh-sinh-nhat-socola-classic", "Thiết kế tối giản hiện đại tone màu be nâu ấm áp, hương vị socola vani cuốn hút.", "Bánh sinh nhật phong cách Hàn Quốc tối giản ấm áp", new BigDecimal("390000"), null, 15, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop", c7, true, false);
    }

    private void createProduct(String name, String slug, String desc, String shortDesc, BigDecimal price, BigDecimal salePrice, int stock, String image, Category category, boolean featured, boolean bestSeller) {
        Product p = Product.builder()
                .productCode(CodeGenerator.generateProductCode())
                .name(name)
                .slug(slug)
                .description(desc)
                .shortDescription(shortDesc)
                .price(price)
                .salePrice(salePrice)
                .stockQuantity(stock)
                .image(image)
                .category(category)
                .status(true)
                .featured(featured)
                .bestSeller(bestSeller)
                .build();
        productRepository.save(p);
    }

    private void seedWorkshops() {
        workshopRepository.save(Workshop.builder()
                .name("Lớp Học Làm Bánh Croissant Ngàn Lớp Chuẩn Pháp")
                .slug("lop-hoc-lam-banh-croissant-phap")
                .description("Tham gia workshop bạn sẽ được Chuyên gia làm bánh hướng dẫn từng bước nhào bột, cán bơ ngàn lớp và nướng ra những chiếc bánh sừng bò giòn rụm ngát hương bơ Elle & Vire.")
                .price(new BigDecimal("650000"))
                .duration("3.5 giờ")
                .capacity(12)
                .registeredCount(4)
                .image("https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop")
                .location("HVTC Kitchen Studio - Số 12 Lê Văn Hiến, Bắc Từ Liêm, Hà Nội")
                .workshopDate(LocalDateTime.now().plusDays(7).withHour(9).withMinute(0))
                .status(true)
                .build());

        workshopRepository.save(Workshop.builder()
                .name("Workshop Trang Trí Bánh Kem Sinh Nhật Korea Style")
                .slug("workshop-trang-tri-banh-kem-korea")
                .description("Học kỹ thuật chà láng bánh kem, pha màu pastel kem tươi Hokkaido và tạo hình họa tiết nghệ thuật theo xu hướng Hàn Quốc hiện đại.")
                .price(new BigDecimal("750000"))
                .duration("4 giờ")
                .capacity(10)
                .registeredCount(6)
                .image("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop")
                .location("HVTC Kitchen Studio - Số 12 Lê Văn Hiến, Bắc Từ Liêm, Hà Nội")
                .workshopDate(LocalDateTime.now().plusDays(14).withHour(14).withMinute(0))
                .status(true)
                .build());

        workshopRepository.save(Workshop.builder()
                .name("Trải Nghiệm Làm Bánh Tiramisu & Mousse Trà Xanh")
                .slug("trai-nghiem-lam-banh-tiramisu-mousse")
                .description("Tự tay pha chế cà phê Espresso, đánh bông kem Mascarpone và hoàn thiện hũ bánh Tiramisu sang xịn cùng bạn bè dịp cuối tuần.")
                .price(new BigDecimal("550000"))
                .duration("3 giờ")
                .capacity(15)
                .registeredCount(8)
                .image("https://images.unsplash.com/photo-1571115177098-24ec42ed204d?w=600&auto=format&fit=crop")
                .location("HVTC Kitchen Studio - Số 12 Lê Văn Hiến, Bắc Từ Liêm, Hà Nội")
                .workshopDate(LocalDateTime.now().plusDays(21).withHour(10).withMinute(0))
                .status(true)
                .build());
    }

    private void seedBlogPosts() {
        blogPostRepository.save(BlogPost.builder()
                .title("Bí Quyết Bảo Quản Bánh Tươi HVTC Giữ Trọn Hương Vị Ngon Như Mới Nướng")
                .slug("bi-quyet-bao-quan-banh-tuoi-hvtc")
                .summary("Hướng dẫn chi tiết cách bảo quản các dòng bánh mì ngàn lớp, bánh Mousse và bánh sinh nhật kem tươi trong tủ lạnh gia đình đúng chuẩn chuyên gia.")
                .content("Bánh tươi tại HVTC BAKERY không sử dụng bất kỳ chất bảo quản nào. Để giữ bánh luôn ngon mượt và thơm phức hương bơ:\n1. Bánh Mousse & Cheesecake: Bảo quản ngăn mát tủ lạnh (2-5°C), bọc kín bằng hộp thủy tinh hoặc màng bọc thực phẩm.\n2. Bánh mì Croissant: Bảo quản nhiệt độ phòng trong 24h, trước khi dùng nướng lại 3 phút ở 160°C trong nồi chiên không dầu.\n3. Bánh sinh nhật kem tươi: Dùng ngon nhất trong vòng 48 giờ sau khi nhận bánh.")
                .image("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600")
                .author("HVTC Chef Team")
                .status(true)
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Hành Trình Chọn Lựa Nguyên Liệu Bơ & Trà Xanh Thượng Hạng Của HVTC Bakery")
                .slug("hanh-trinh-chon-lua-nguyen-lieu-thua-hang")
                .summary("Khám phá nguồn gốc bơElle & Vire nhập khẩu Pháp và bột trà xanh Uji cao cấp Nhật Bản tạo nên nét ấm áp tinh tế trong từng ổ bánh HVTC.")
                .content("Tại HVTC BAKERY, chúng tôi tin rằng một chiếc bánh ngon bắt đầu từ những nguyên liệu tử tế nhất. Bơ lạt Elle & Vire từ vùng Normandy nước Pháp mang lại độ béo thơm đậm đà không ngấy, cùng bột trà xanh Matcha Uji sấy lạnh bảo toàn màu xanh ngọc tươi nguyên và vị đắng ngọt hài hòa.")
                .image("https://images.unsplash.com/photo-1546039907-7fa05f864c02?w=600&auto=format&fit=crop")
                .author("Ban Biên Tập HVTC")
                .status(true)
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Gợi Ý Các Mẫu Bánh Tiệc Sinh Nhật Sang Trọng & Ấm Áp Cho Gia Đình")
                .slug("goi-y-cac-mau-banh-tiec-sinh-nhat-sang-trong")
                .summary("Tổng hợp những mẫu bánh tiệc sinh nhật tone kem be ấm áp, trang trí hoa tươi và dâu mọng được yêu thích nhất mùa này.")
                .content("Tiệc sinh nhật là khoảnh khắc gắn kết tuyệt vời của gia đình. Với phong cách thiết kế tối giản, thanh lịch, chiếc bánh sinh nhật HVTC BAKERY không chỉ là món quà ngọt ngào mà còn là điểm nhấn chụp ảnh kỷ niệm rực rỡ.")
                .image("https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=600")
                .author("HVTC Stylist")
                .status(true)
                .build());
    }
}
