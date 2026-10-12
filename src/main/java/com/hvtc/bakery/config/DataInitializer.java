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

import org.springframework.jdbc.core.JdbcTemplate;
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
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Khởi tạo dữ liệu mẫu cho HVTC BAKERY...");

        // Drop legacy check constraints on orders table if present (e.g. CONSTRAINT_8B created prior to adding PAYOS enum)
        try {
            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS CONSTRAINT_8B");
            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS CONSTRAINT_8");
        } catch (Exception ex) {
            log.warn("Bypass lỗi khi xử lý bảng orders: {}", ex.getMessage());
        }

        // Seed default Welcome Coupon HVTC
        if (!couponRepository.existsByCodeIgnoreCase("HVTC")) {
            couponRepository.save(Coupon.builder()
                    .code("HVTC")
                    .discountPercent(10)
                    .minOrderAmount(BigDecimal.ZERO)
                    .active(true)
                    .build());
            log.info("Đã tạo mã giảm giá chào mừng mẫu thành công: HVTC (Giảm 10%)");
        }

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

        // 4. Categories & Products (5-7 products per category)
        seedCategoriesAndProducts();

        // 5. Workshops
        if (workshopRepository.count() == 0) {
            seedWorkshops();
        }

        // 6. Blog Posts
        if (blogPostRepository.count() == 0) {
            seedBlogPosts();
        }

        // 7. Sample Orders for Revenue Analysis
        if (orderRepository.count() == 0) {
            seedSampleOrders();
        }

        log.info("Hoàn tất khởi tạo dữ liệu mẫu cho HVTC BAKERY!");
    }

    private void seedCategoriesAndProducts() {
        Category c1 = getOrCreateCategory("Bánh mì", "banh-mi", "Các loại bánh mì tươi thơm nức, nướng mới mỗi sáng", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600");
        Category c2 = getOrCreateCategory("Bánh ngọt", "banh-ngot", "Bánh ngọt Pháp, Mousse thanh nhẹ và Cheesecake béo ngậy", "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600");
        Category c3 = getOrCreateCategory("Bánh quy", "banh-quy", "Bánh quy bơ giòn rụm, bánh quy hạt ngũ cơ cao cấp", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600");
        Category c4 = getOrCreateCategory("Socola", "socola", "Socola tươi handmade và truffles đắng ngọt đậm đà", "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600");
        Category c5 = getOrCreateCategory("Đồ uống", "do-uong", "Trà hoa quả tươi, cà phê pha máy và trà sữa thơm ngậy", "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600");
        Category c6 = getOrCreateCategory("Set bánh", "set-banh", "Hộp quà bánh tươi sang trọng dành tặng đối tác và gia đình", "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=600&auto=format&fit=crop");
        Category c7 = getOrCreateCategory("Bánh sinh nhật", "banh-sinh-nhat", "Bánh kem tiệc sinh nhật thiết kế riêng, mẫu mã tinh tế", "https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600&auto=format&fit=crop");

        // --- 1. BÁNH MÌ (6 items) ---
        createProductIfAbsent("Bánh Croissant Bơ Pháp", "banh-croissant-bo-phap", "Bánh sừng bò nướng ngàn lớp với bơ Elle & Vire thơm lừng, ngàn lớp giòn tan xốp mịn.", "Croissant ngàn lớp giòn thơm bơ Pháp tươi", new BigDecimal("45000"), new BigDecimal("39000"), 50, "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600", c1, false, true);
        createProductIfAbsent("Bánh Mì Garlic Butter (Bơ Tỏi)", "banh-mi-garlic-butter", "Bánh mì bơ tỏi Hàn Quốc nhân kem phô mai thơm phảng phất hương tỏi nướng quyến rũ.", "Bánh mì bơ tỏi phô mai Hàn Quốc sốt béo ngậy", new BigDecimal("55000"), new BigDecimal("48000"), 45, "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=600", c1, false, true);
        createProductIfAbsent("Bánh Mì Pain au Chocolat", "banh-mi-pain-au-chocolat", "Bánh mì cuộn socola đắng ngàn lớp truyền thống Pháp giòn rụm tan trong miệng.", "Pain au Chocolat nướng mới thơm nức thanh socola", new BigDecimal("48000"), null, 40, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600", c1, false, false);
        createProductIfAbsent("Bánh Sandwich Sourdough Nguyên Cám", "banh-sandwich-sourdough", "Bánh mì men tự nhiên Sourdough nguyên cám tốt cho sức khỏe, vị chua nhẹ thanh mát.", "Sandwich Sourdough men tự nhiên nguyên cám", new BigDecimal("65000"), new BigDecimal("55000"), 30, "https://images.unsplash.com/photo-1586444248902-2f64eddc13df?w=600&auto=format&fit=crop", c1, true, false);
        createProductIfAbsent("Bánh Baguette Pháp Truyền Thống", "banh-baguette-phap", "Bánh baguette chuẩn phong cách Pháp với vỏ ngoài giòn rụm và ruột mềm xốp đặc trưng.", "Baguette Pháp truyền thống vỏ giòn ruột mềm", new BigDecimal("35000"), new BigDecimal("30000"), 60, "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?w=600&auto=format&fit=crop", c1, false, false);
        createProductIfAbsent("Bánh Mì Gối Brioche Bơ Thượng Hạng", "banh-mi-brioche-bo", "Bánh mì Brioche đẫm bơ Elle & Vire thơm béo, sớ bánh dai mềm tan trong miệng.", "Bánh mì Brioche hoàng gia đậm đà vị bơ", new BigDecimal("72000"), new BigDecimal("65000"), 25, "https://images.unsplash.com/photo-1608198093002-ad4e005484ec?w=600&auto=format&fit=crop", c1, true, true);

        // --- 2. BÁNH NGỌT (7 items) ---
        createProductIfAbsent("Bánh Mousse Matcha Uji", "banh-mousse-matcha", "Mousse trà xanh Uji Nhật Bản kết hợp cốt bánh gato mịn mượt, vị ngọt dịu nhẹ đắng nhẹ đầu lưỡi.", "Mousse trà xanh Nhật Bản thanh mát ít ngọt", new BigDecimal("280000"), new BigDecimal("250000"), 25, "https://images.unsplash.com/photo-1546039907-7fa05f864c02?w=600&auto=format&fit=crop", c2, true, true);
        createProductIfAbsent("Bánh Tiramisu Chuẩn Vị Ý", "banh-tiramisu", "Bánh Tiramisu phong cách Ý truyền thống với cốt bánh Savoiardi thấm đẫm cà phê Espresso và rượu Kahlua.", "Tiramisu chuẩn vị Ý béo ngậy thơm nức cà phê", new BigDecimal("320000"), new BigDecimal("290000"), 30, "https://images.unsplash.com/photo-1571115177098-24ec42ed204d?w=600&auto=format&fit=crop", c2, true, true);
        createProductIfAbsent("Bánh Red Velvet Velvet Cream Cheese", "banh-red-velvet", "Bánh nhung đỏ kiêu kỳ phủ kem phô mai Cream Cheese chua ngọt nhẹ nhàng quyến rũ.", "Bánh nhung đỏ nồng nàn thơm phức kem phô mai", new BigDecimal("350000"), new BigDecimal("320000"), 20, "https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=600&auto=format&fit=crop", c2, true, false);
        createProductIfAbsent("Bánh Bông Lan Trứng Muối Hoàng Kim", "banh-bong-lan-trung-muoi", "Cốt bánh bông lan xốp mềm, sốt phô mai hoàng kim béo ngậy cùng trứng muối chín tới thơm bùi và chà bông thượng hạng.", "Bông lan trứng muối đượm vị mặn ngọt hài hòa", new BigDecimal("180000"), new BigDecimal("160000"), 35, "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=600&auto=format&fit=crop", c2, true, true);
        createProductIfAbsent("Bánh Cheesecake Việt Quất Dại", "banh-cheesecake-viet-quat", "Phô mai tươi nướng chậm hòa quyện mứt việt quất dại chua ngọt mượt mát lạnh.", "New York Cheesecake việt quất dại ngậy béo", new BigDecimal("310000"), new BigDecimal("280000"), 22, "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=600", c2, true, true);
        createProductIfAbsent("Bánh Choux Vani Hoàng Gia", "banh-choux-vani", "Bánh su kem nhân kem trứng vani Madagascar béo thơm căng tròn mát lạnh.", "Bánh su kem vỏ giòn nhân vani thơm phức", new BigDecimal("65000"), new BigDecimal("58000"), 40, "https://images.unsplash.com/photo-1550617931-e17a7b70dce2?w=600&auto=format&fit=crop", c2, false, false);
        createProductIfAbsent("Bánh Tart Trái Cây Mùa Hè", "banh-tart-trai-cay", "Vỏ bánh tart bơ giòn ruộm chứa kem custard nhẹ và xếp tràn đầy quả mọng.", "Tart trái cây cầu vồng rực rỡ tươi mát", new BigDecimal("75000"), new BigDecimal("65000"), 35, "https://images.unsplash.com/photo-1519869325930-281384150729?w=600", c2, false, true);

        // --- 3. BÁNH QUY (6 items) ---
        createProductIfAbsent("Bánh Cookie Bơ Hạnh Nhân", "banh-cookie-bo-hanh-nhan", "Bánh quy bơ giòn rụm kết hợp hạt hạnh nhân nướng thơm phức, lựa chọn lý tưởng dùng cùng trà chiều.", "Cookie bơ giòn ngậy rắc hạnh nhân thơm lừng", new BigDecimal("85000"), new BigDecimal("75000"), 40, "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=600&auto=format&fit=crop", c3, false, false);
        createProductIfAbsent("Hộp Bánh Macaron Pháp (6 cái)", "banh-macaron-phap", "Hộp 6 bánh Macaron phong cách Pháp đủ sắc màu vị chanh leo, pistacchio, rasberry, matcha, chocolate, vani.", "Bánh Macaron kiêu kỳ rực rỡ chuẩn vị Paris", new BigDecimal("190000"), new BigDecimal("175000"), 25, "https://images.unsplash.com/photo-1569864358642-9d1684040f43?w=600", c3, true, true);
        createProductIfAbsent("Bánh Quy Socola Chip Mỹ", "banh-quy-socola-chip", "Bánh quy socola chip nướng giòn viền đậm vị ca-cao Mỹ hòa quyện bơ tươi thanh ngậy.", "Bánh quy Chocolate Chip nồng nàn vị cacao", new BigDecimal("95000"), new BigDecimal("85000"), 45, "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&auto=format&fit=crop", c3, false, true);
        createProductIfAbsent("Bánh Quy Yến Mạch Hạt Dinh Dưỡng", "banh-quy-yen-mach-hat", "Bánh quy yến mạch nướng mật ong kết hợp hạt bí, hạt điều và nho khô ngọt thanh an toàn.", "Bánh quy yến mạch ăn kiêng bổ dưỡng", new BigDecimal("110000"), new BigDecimal("99000"), 35, "https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=600&auto=format&fit=crop", c3, true, false);
        createProductIfAbsent("Bánh Quy Bơ Ngàn Lớp Palmier", "banh-quy-bo-palmier", "Bánh quy trái tim ngàn lớp nướng caramen bơ giòn rụm đặc sản nước Pháp.", "Bánh quy bơ ngàn lớp hình trái tim giòn tan", new BigDecimal("90000"), new BigDecimal("78000"), 30, "https://images.unsplash.com/photo-1621236378699-8597faf6a176?w=600&auto=format&fit=crop", c3, false, false);
        createProductIfAbsent("Hộp Bánh Quy Lưỡi Mèo Vani & Socola", "banh-quy-luoi-meo", "Hộp bánh quy lưỡi mèo mỏng giòn thơm ngậy hương vani tự nhiên và lớp viền socola đắng nhẹ.", "Bánh quy lưỡi mèo giòn tan ngát hương vani", new BigDecimal("125000"), new BigDecimal("110000"), 28, "https://images.unsplash.com/photo-1530610476181-d83430b64dcd?w=600&auto=format&fit=crop", c3, false, true);

        // --- 4. SOCOLA (6 items) ---
        createProductIfAbsent("Bánh Chocolate Truffle 70% Cacao", "banh-chocolate-truffle", "Cốt bánh socola nguyên chất 70% cacao đến từ Bỉ, phủ ganache socola mượt mà béo đậm.", "Socola Bỉ đắng dịu hòa quyện lớp kem béo ngậy", new BigDecimal("360000"), new BigDecimal("320000"), 18, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600", c4, true, true);
        createProductIfAbsent("Socola Tươi Nama Chocolate", "socola-tuoi-nama", "Socola tươi phủ bột cacao mỏng mềm tan chảy tức thì khi đưa vào miệng.", "Nama Chocolate nguyên chất mềm mịn cực đỉnh", new BigDecimal("220000"), new BigDecimal("195000"), 30, "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600", c4, true, false);
        createProductIfAbsent("Socola Bonbon Nghệ Thuật (Hộp 9 viên)", "socola-bonbon-nghe-thuat", "Hộp 9 viên Socola Bonbon nhân chanh leo, caramel muối và rượu Baileys vẽ tay tỉ mỉ.", "Socola Bonbon nghệ thuật quà tặng sang trọng", new BigDecimal("250000"), new BigDecimal("225000"), 20, "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=600&auto=format&fit=crop", c4, true, true);
        createProductIfAbsent("Thanh Socola Đen Hạt Dẻ Cười & Việt Quất", "thanh-socola-den-hat-de", "Socola đen 75% cacao rắc hạt dẻ cười nướng giòn cùng việt quất sấy dẻo thơm mát.", "Thanh Socola đen hạt dẻ cười cao cấp", new BigDecimal("140000"), new BigDecimal("125000"), 40, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop", c4, false, false);
        createProductIfAbsent("Hộp Truffles Socola Rượu Rhum & Vani", "truffles-socola-ruou-rhum", "Viên Truffles socola đắng ngâm rượu Rhum nồng nàn phủ vụn bánh quy đượm đà quyến rũ.", "Truffles Socola rượu Rhum thượng hạng", new BigDecimal("290000"), new BigDecimal("260000"), 15, "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=600&auto=format&fit=crop", c4, true, false);
        createProductIfAbsent("Kẹo Socola Caramel Muối Biển", "socola-caramel-muoi-bien", "Viên kẹo socola bọc nhân caramel chảy dẻo quyện muối biển pink Himalaya thanh mặn.", "Socola nhân Caramel muối biển dẻo mịn", new BigDecimal("165000"), new BigDecimal("145000"), 35, "https://images.unsplash.com/photo-1548907040-4baa42d10919?w=600&auto=format&fit=crop", c4, false, true);

        // --- 5. ĐỒ UỐNG (6 items) ---
        createProductIfAbsent("Trà Matcha Latte Kem Béo", "tra-matcha-latte", "Matcha Nhật Bản pha sữa tươi nguyên chất lớp kem béo mặn đượm đà thanh mát.", "Đồ uống Matcha thượng hạng dùng kèm bánh ngọt", new BigDecimal("52000"), new BigDecimal("45000"), 60, "https://images.unsplash.com/photo-1515823662972-da6a2e4d3002?w=600&auto=format&fit=crop", c5, false, false);
        createProductIfAbsent("Cà Phê Cold Brew Cam Sả", "ca-phe-cold-brew-cam-sa", "Cà phê ủ lạnh 18 tiếng ngâm tinh dầu cam vàng và sả tươi thanh lọc sảng khoái.", "Cà phê Cold Brew vị trái cây tươi sáng độc đáo", new BigDecimal("58000"), new BigDecimal("49000"), 50, "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600", c5, false, true);
        createProductIfAbsent("Trà Đào Cam Sả Tươi Mát", "tra-dao-cam-sa", "Trà đen ủ ấm kết hợp miếng đào mọng nước, hương sả phảng phất và vị chua dịu cam vàng.", "Trà đào cam sả thanh nhiệt giải khát", new BigDecimal("48000"), new BigDecimal("42000"), 70, "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=600&auto=format&fit=crop", c5, false, true);
        createProductIfAbsent("Cà Phê Latte Bọt Sữa Hokkaido", "ca-phe-latte-hokkaido", "Espresso 2 shot cà phê Arabica cầu đất kết hợp sữa tươi Hokkaido bọt mịn như mây.", "Latte bọt sữa Hokkaido thơm đượm vị cà phê", new BigDecimal("55000"), new BigDecimal("48000"), 45, "https://images.unsplash.com/photo-1534778101976-62847782c213?w=600&auto=format&fit=crop", c5, true, false);
        createProductIfAbsent("Trà Dâu Tây Dừa Lưới Tươi", "tra-dau-tay-dua-luoi", "Trà nhài ủ lạnh lắc dâu tây Đà Lạt dầm tươi cùng nước dừa xiêm ngọt lịm tự nhiên.", "Trà dâu tây dừa tươi mát lạnh giải nhiệt", new BigDecimal("56000"), new BigDecimal("49000"), 55, "https://images.unsplash.com/photo-1546173159-315724a31696?w=600&auto=format&fit=crop", c5, false, false);
        createProductIfAbsent("Sinh Tố Xoài Dừa Hạt Chia", "sinh-to-xoai-dua-hat-chia", "Xoài chín cây xay mịn với sữa dừa béo ngậy, rắc hạt chia hữu cơ giòn vui miệng.", "Sinh tố xoài dừa hạt chia bổ dưỡng detox", new BigDecimal("62000"), new BigDecimal("55000"), 40, "https://images.unsplash.com/photo-1553530666-ba11a7da3888?w=600&auto=format&fit=crop", c5, true, true);

        // --- 6. SET BÁNH (6 items) ---
        createProductIfAbsent("Set Quà Bánh Petit HVTC (9 chiếc)", "set-qua-banh-petit-hvtc", "Set 9 chiếc bánh ngọt mini xinh xắn bao gồm Tart trái cây, Choux vani và Opera cake.", "Set bánh tiệc trà chiều cao cấp thích hợp làm quà", new BigDecimal("380000"), new BigDecimal("350000"), 10, "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=600&auto=format&fit=crop", c6, true, true);
        createProductIfAbsent("Set Bánh Tiệc Trà Chiều Tea Party Set", "set-banh-tiec-tra-chieu", "Hộp quà tiệc trà hoàng gia gồm bánh Scone, Tart mặn, Macaron và hũ mứt dâu tây cao cấp.", "Tea Party Set đượm phong cách quý tộc Anh", new BigDecimal("450000"), new BigDecimal("399000"), 15, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600", c6, true, true);
        createProductIfAbsent("Set Bánh Croissant & Danish Mini (6 chiếc)", "set-banh-croissant-danish-mini", "Set 6 bánh ngàn lớp mini gồm Croissant bơ, Danish táo, Danish mâm xôi thơm giòn tan.", "Set bánh mì ngọt ngàn lớp mini ăn sáng", new BigDecimal("260000"), new BigDecimal("230000"), 20, "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600", c6, false, false);
        createProductIfAbsent("Hộp Quà Bánh Macaron & Socola Cao Cấp", "hop-qua-macaron-socola", "Hộp quà sang trọng thiết kế nơ lụa gồm 6 bánh Macaron và 6 viên Socola Bonbon vẽ tay.", "Hộp quà Macaron & Socola kiều diễm tặng người thương", new BigDecimal("490000"), new BigDecimal("450000"), 12, "https://images.unsplash.com/photo-1569864358642-9d1684040f43?w=600", c6, true, false);
        createProductIfAbsent("Set Bánh Tart Trái Cây Đa Sắc (6 chiếc)", "set-banh-tart-trai-cay", "Set 6 bánh Tart trái cây tươi gồm việt quất, dâu tây, ki-wi, dâu tằm ngọt dịu mát lạnh.", "Hộp 6 bánh Tart trái cây tươi rực rỡ sắc màu", new BigDecimal("390000"), new BigDecimal("360000"), 18, "https://images.unsplash.com/photo-1519869325930-281384150729?w=600", c6, false, true);
        createProductIfAbsent("Set Bánh Sinh Nhật Mousse & Hoa Tươi", "set-banh-mousse-hoa-tuoi", "Bánh Mousse trà xanh mini kèm hộp hoa hồng tươi tinh tế trao gửi lời chúc chân thành.", "Combo bánh sinh nhật mini kết hợp hoa tươi", new BigDecimal("520000"), new BigDecimal("480000"), 10, "https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600", c6, true, true);

        // --- 7. BÁNH SINH NHẬT (6 items) ---
        createProductIfAbsent("Bánh Sinh Nhật Dâu Tây Kem Tươi", "banh-sinh-nhat-dau-tay", "Bánh sinh nhật kem tươi hái tận vườn Đà Lạt, trang trí dâu tươi mọng ngọt lịm tim.", "Bánh sinh nhật dâu tây tươi kem Hokkaido béo mịn", new BigDecimal("420000"), new BigDecimal("390000"), 15, "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600&auto=format&fit=crop", c7, true, true);
        createProductIfAbsent("Bánh Kem Socola Trái Cây Quả Mọng", "banh-kem-socola-trai-cay", "Bánh sinh nhật cốt vani Socola kết hợp việt quất và ki-wi tươi tạo hương vị thanh lịch.", "Bánh tiệc sinh nhật sang trọng vị socola quả mọng", new BigDecimal("450000"), new BigDecimal("410000"), 12, "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600", c7, true, false);
        createProductIfAbsent("Bánh Sinh Nhật Socola Vani Classic", "banh-sinh-nhat-socola-classic", "Thiết kế tối giản hiện đại tone màu be nâu ấm áp, hương vị socola vani cuốn hút.", "Bánh sinh nhật phong cách Hàn Quốc tối giản ấm áp", new BigDecimal("390000"), new BigDecimal("350000"), 15, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop", c7, true, false);
        createProductIfAbsent("Bánh Sinh Nhật Bông Lan Trứng Muối Vương Miện", "banh-sinh-nhat-trung-muoi-vuong-mien", "Bánh tiệc sinh nhật bông lan trứng muối phô mai tan chảy đính vương miện lấp lánh sang chảnh.", "Bánh sinh nhật trứng muối vương miện hoành tráng", new BigDecimal("480000"), new BigDecimal("430000"), 10, "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=600&auto=format&fit=crop", c7, true, true);
        createProductIfAbsent("Bánh Kem Mousse Chanh Dây & Dừa", "banh-kem-mousse-chanh-day", "Cốt mousse chanh dây thanh mát phủ lớp dừa sấy thơm lừng, hoa kem trắng tinh khôi.", "Bánh sinh nhật Mousse chanh dây chua ngọt tươi mát", new BigDecimal("410000"), new BigDecimal("370000"), 14, "https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600&auto=format&fit=crop", c7, false, false);
        createProductIfAbsent("Bánh Sinh Nhật Bento Mini Hàn Quốc", "banh-sinh-nhat-bento-mini", "Chiếc bánh Bento xinh xắn size 10cm trang trí chữ vẽ tay ngọt ngào đựng trong hộp giấy thân thiện.", "Bánh kem Bento xinh xắn nhắn gửi yêu thương", new BigDecimal("250000"), new BigDecimal("220000"), 25, "https://images.unsplash.com/photo-1621303837174-89787a7d4729?w=600&auto=format&fit=crop", c7, true, true);
    }

    private Category getOrCreateCategory(String name, String slug, String description, String image) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .slug(slug)
                        .description(description)
                        .image(image)
                        .status(true)
                        .build()));
    }

    private void createProductIfAbsent(String name, String slug, String desc, String shortDesc, BigDecimal price, BigDecimal salePrice, int stock, String image, Category category, boolean featured, boolean bestSeller) {
        if (!productRepository.existsBySlug(slug)) {
            createProduct(name, slug, desc, shortDesc, price, salePrice, stock, image, category, featured, bestSeller);
        }
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

    private void seedSampleOrders() {
        var products = productRepository.findAll();
        if (products.isEmpty()) return;

        User sampleUser = userRepository.findByUsername("user").orElse(null);

        // Generate sample orders over the last 15 days
        for (int i = 15; i >= 0; i--) {
            LocalDateTime orderDate = LocalDateTime.now().minusDays(i).withHour(9 + (i % 8)).withMinute(15 * (i % 4));
            
            // Create 1 or 2 orders per day
            int orderCount = (i % 3 == 0) ? 2 : 1;
            for (int k = 0; k < orderCount; k++) {
                Product p1 = products.get((i + k) % products.size());
                Product p2 = products.get((i + k + 3) % products.size());

                BigDecimal price1 = p1.getEffectivePrice();
                BigDecimal price2 = p2.getEffectivePrice();

                int qty1 = 1 + (i % 2);
                int qty2 = 1;

                BigDecimal subtotal1 = price1.multiply(BigDecimal.valueOf(qty1));
                BigDecimal subtotal2 = price2.multiply(BigDecimal.valueOf(qty2));
                BigDecimal total = subtotal1.add(subtotal2);

                OrderStatus status = (i == 0) ? OrderStatus.PENDING : (i == 1 ? OrderStatus.SHIPPING : (i % 5 == 0 ? OrderStatus.CANCELLED : OrderStatus.COMPLETED));
                PaymentStatus payStatus = (status == OrderStatus.COMPLETED || status == OrderStatus.SHIPPING) ? PaymentStatus.PAID : PaymentStatus.UNPAID;

                Order order = Order.builder()
                        .orderCode("HVTC-ORD-" + (1000 + i * 2 + k))
                        .user(sampleUser)
                        .customerName("Khách Hàng " + (i + 1))
                        .phone("098" + (1000000 + i * 1111))
                        .email("customer" + (i + 1) + "@gmail.com")
                        .address("Số " + (10 + i) + " Đường Đức Thắng, Bắc Từ Liêm, Hà Nội")
                        .note("Giao giờ hành chính")
                        .totalAmount(total)
                        .shippingFee(new BigDecimal("20000"))
                        .discountAmount(i % 4 == 0 ? new BigDecimal("20000") : BigDecimal.ZERO)
                        .paymentMethod(PaymentMethod.COD)
                        .paymentStatus(payStatus)
                        .orderStatus(status)
                        .createdAt(orderDate)
                        .items(new java.util.ArrayList<>())
                        .build();

                OrderItem item1 = OrderItem.builder()
                        .order(order)
                        .product(p1)
                        .quantity(qty1)
                        .price(price1)
                        .subtotal(subtotal1)
                        .build();

                OrderItem item2 = OrderItem.builder()
                        .order(order)
                        .product(p2)
                        .quantity(qty2)
                        .price(price2)
                        .subtotal(subtotal2)
                        .build();

                order.getItems().add(item1);
                order.getItems().add(item2);

                orderRepository.save(order);
            }
        }
        log.info("Đã khởi tạo đơn hàng mẫu cho thống kê doanh thu thành công!");
    }
}
