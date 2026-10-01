package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.ChatRequest;
import com.hvtc.bakery.dto.ChatResponse;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.entity.Workshop;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.repository.WorkshopRepository;
import com.hvtc.bakery.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AIChatService implements ChatbotService {

    private final ProductRepository productRepository;
    private final WorkshopRepository workshopRepository;

    @Value("${hvtc.ai.chatbot.api-key:demo-hvtc-bakery-key}")
    private String apiKey;

    @Value("${hvtc.ai.chatbot.model:gpt-3.5-turbo}")
    private String modelName;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    public ChatResponse processUserMessage(ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ChatResponse.builder()
                    .reply("HVTC BAKERY xin chào! Bạn cần hỗ trợ chọn bánh, tư vấn lớp học workshop hay thông tin cửa hàng ạ?")
                    .suggestedProducts(List.of())
                    .build();
        }

        String rawQuery = request.getMessage().trim();
        String query = rawQuery.toLowerCase(Locale.ROOT);

        List<ChatResponse.ProductSummaryDto> suggestions = new ArrayList<>();
        String reply;

        // 1. Intent: Baking Workshop / Classes (Lớp học làm bánh, Workshop)
        if (query.contains("lớp học") || query.contains("lop hoc") 
         || query.contains("workshop") || query.contains("dạy làm bánh") || query.contains("day lam banh")
         || query.contains("học làm bánh") || query.contains("hoc lam banh") || query.contains("khoá học") || query.contains("khoa hoc")) {

            List<Workshop> workshops = workshopRepository.findByStatusTrueOrderByWorkshopDateAsc();
            reply = "HVTC BAKERY có mở các lớp học Workshop làm bánh trực tiếp tại Kitchen Studio với Chuyên gia làm bánh. Dưới đây là danh sách các lớp học sắp khai giảng dành cho bạn:";
            
            suggestions = workshops.stream()
                    .limit(4)
                    .map(ws -> ChatResponse.ProductSummaryDto.builder()
                            .name(ws.getName())
                            .slug("workshop/" + ws.getSlug())
                            .image(ws.getImage() != null ? ws.getImage() : "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600")
                            .priceFormatted(CURRENCY_FORMAT.format(ws.getPrice()) + " • " + ws.getDuration())
                            .build())
                    .toList();

            return ChatResponse.builder()
                    .reply(reply)
                    .suggestedProducts(suggestions)
                    .build();
        }

        // 2. Intent: Address, Phone & Contact Info (Địa chỉ, SĐT, Giờ mở cửa)
        if (query.contains("địa chỉ") || query.contains("dia chi") || query.contains("ở đâu") || query.contains("o dau")
         || query.contains("sđt") || query.contains("sdt") || query.contains("số điện thoại") || query.contains("so dien thoai")
         || query.contains("hotline") || query.contains("liên hệ") || query.contains("lien he") || query.contains("mở cửa") || query.contains("mo cua")
         || query.contains("tiệm") || query.contains("quán")) {

            reply = "📍 **Thông Tin Cửa Hàng & Liên Hệ HVTC BAKERY:**\n\n" +
                    "• **Địa chỉ Studio:** Số 12 Phố Lê Văn Hiến, Phường Đức Thắng, Quận Bắc Từ Liêm, Hà Nội (gần Học viện Tài chính)\n" +
                    "• **Hotline tư vấn & đặt bánh:** **0964740141**\n" +
                    "• **Email CSKH:** cskh@hvtcbakery.vn\n" +
                    "• **Giờ mở cửa:** 07:00 - 21:30 hàng ngày (kể cả Thứ 7, Chủ Nhật)";

            return ChatResponse.builder()
                    .reply(reply)
                    .suggestedProducts(List.of())
                    .build();
        }

        // 3. Intent: Delivery Time & Order Preparation Time (Thời gian giao hàng, nướng bánh)
        if (query.contains("giao hàng") || query.contains("giao hang") || query.contains("ship")
         || query.contains("bao lâu") || query.contains("bao lau") || query.contains("thời gian") || query.contains("thoi gian")
         || query.contains("làm bánh") || query.contains("lam banh") || query.contains("đặt trước") || query.contains("dat truoc")) {

            reply = "🚚 **Thông tin Giao Hàng & Thời gian Làm Bánh:**\n\n" +
                    "• **Thời gian giao hàng:** HVTC BAKERY hỗ trợ giao bánh hỏa tốc trong **1 - 2 giờ** đối với khu vực Nội thành Hà Nội và **2 - 4 giờ** đối với các khu vực xa hơn.\n\n" +
                    "• **Thời gian chuẩn bị/làm bánh:** Bánh mì & bánh ngọt tươi được nướng mới mỗi sáng tại tiệm. Đối với bánh tiệc/sinh nhật thiết kế theo yêu cầu riêng, bạn vui lòng **đặt trước 2 - 4 tiếng** để đầu bếp chuẩn bị chu đáo và tươi ngon nhất!";

            return ChatResponse.builder()
                    .reply(reply)
                    .suggestedProducts(List.of())
                    .build();
        }

        // 4. Intent: Product Search & Natural Language Price Filter
        List<Product> allProducts = productRepository.findByStatusTrue();
        List<Product> matchedProducts = new ArrayList<>();

        BigDecimal maxPriceFilter = extractMaxPrice(query);
        BigDecimal minPriceFilter = extractMinPrice(query);
        String matchedCategorySlug = detectCategorySlug(query);
        String categoryDisplayName = detectCategoryDisplayName(query);

        // Scenario A: Price filtering
        if (maxPriceFilter != null || minPriceFilter != null) {
            final BigDecimal maxP = maxPriceFilter;
            final BigDecimal minP = minPriceFilter;

            matchedProducts = allProducts.stream()
                    .filter(p -> (matchedCategorySlug == null || p.getCategory().getSlug().equalsIgnoreCase(matchedCategorySlug)))
                    .filter(p -> maxP == null || p.getEffectivePrice().compareTo(maxP) <= 0)
                    .filter(p -> minP == null || p.getEffectivePrice().compareTo(minP) >= 0)
                    .sorted(Comparator.comparing(Product::getEffectivePrice))
                    .toList();

            String priceLabel = "";
            if (maxP != null && minP != null) {
                priceLabel = "từ " + formatVnd(minP) + " đến " + formatVnd(maxP);
            } else if (maxP != null) {
                priceLabel = "dưới " + formatVnd(maxP);
            } else {
                priceLabel = "từ " + formatVnd(minP);
            }

            if (!matchedProducts.isEmpty()) {
                reply = "Dưới đây là danh sách " + (categoryDisplayName != null ? categoryDisplayName.toLowerCase() : "bánh tươi") +
                        " có mức giá " + priceLabel + " tại HVTC BAKERY phù hợp với yêu cầu của bạn:";
            } else {
                matchedProducts = allProducts.stream()
                        .filter(p -> (matchedCategorySlug == null || p.getCategory().getSlug().equalsIgnoreCase(matchedCategorySlug)))
                        .sorted(Comparator.comparing(Product::getEffectivePrice))
                        .limit(4)
                        .toList();
                reply = "Hiện tại dòng " + (categoryDisplayName != null ? categoryDisplayName.toLowerCase() : "bánh") +
                        " có giá " + priceLabel + " chưa có sẵn. Bạn có thể tham khảo các sản phẩm bánh có giá tiết kiệm nhất dưới đây:";
            }
        }
        // Scenario B: Category Search Intent
        else if (matchedCategorySlug != null) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getCategory().getSlug().equalsIgnoreCase(matchedCategorySlug))
                    .toList();
            reply = "HVTC BAKERY có nhiều mẫu " + categoryDisplayName + " thơm ngon, trang trí tinh tế. Dưới đây là các sản phẩm nổi bật dành cho bạn:";
        }
        // Scenario C: Special preferences
        else if (query.contains("ít ngọt") || query.contains("it ngot") || query.contains("ăn kiêng") || query.contains("kieng")) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("matcha") 
                              || p.getName().toLowerCase().contains("cheesecake")
                              || p.getName().toLowerCase().contains("tiramisu"))
                    .toList();
            reply = "Nếu bạn ưa thích vị bánh dịu nhẹ, thanh mát và ít ngọt, HVTC BAKERY gợi ý các dòng bánh Mousse Matcha, Cheesecake hoặc Tiramisu béo nhẹ:";
        } else if (query.contains("quà") || query.contains("qua") || query.contains("biếu") || query.contains("tặng")) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("cookie") 
                              || p.getName().toLowerCase().contains("macaron")
                              || p.getCategory().getSlug().equals("set-banh"))
                    .toList();
            reply = "Để làm quà tặng tinh tế và ấm áp cho người thân, đối tác, các set Bánh Cookie Bơ cao cấp hoặc Hộp Bánh Macaron phong cách Pháp tại HVTC BAKERY là lựa chọn tuyệt vời:";
        } else if (query.contains("bảo quản") || query.contains("bao quan") || query.contains("hạn sử dụng")) {
            reply = "Bánh tại HVTC BAKERY đều được làm mới mỗi ngày không dùng chất bảo quản. Bạn nên bảo quản bánh trong ngăn mát tủ lạnh (2-6°C) và dùng tốt nhất trong vòng 24 - 48 giờ để giữ trọn hương vị tươi ngon!";
        } else {
            String cleanKey = query.replaceAll("(?i)(tôi|muốn|mua|tìm|kiếm|cho|hỏi|xem|cần|có|bánh)", "").trim();
            if (!cleanKey.isEmpty()) {
                matchedProducts = allProducts.stream()
                        .filter(p -> p.getName().toLowerCase().contains(cleanKey) || (p.getDescription() != null && p.getDescription().toLowerCase().contains(cleanKey)))
                        .toList();
            }
            if (matchedProducts.isEmpty()) {
                matchedProducts = productRepository.findByBestSellerTrueAndStatusTrue();
                reply = "HVTC BAKERY xin cảm ơn bạn đã nhắn tin! Chúng tôi tự hào mang đến các sản phẩm bánh tươi 100% nguyên liệu tự nhiên mỗi ngày. Dưới đây là những sản phẩm bán chạy nhất bạn có thể thử:";
            } else {
                reply = "HVTC BAKERY đã tìm thấy danh sách sản phẩm phù hợp với yêu cầu của bạn:";
            }
        }

        suggestions = matchedProducts.stream()
                .limit(4)
                .map(p -> ChatResponse.ProductSummaryDto.builder()
                        .name(p.getName())
                        .slug(p.getSlug())
                        .image(p.getImage())
                        .priceFormatted(CURRENCY_FORMAT.format(p.getEffectivePrice()))
                        .build())
                .toList();

        return ChatResponse.builder()
                .reply(reply)
                .suggestedProducts(suggestions)
                .build();
    }

    private String formatVnd(BigDecimal price) {
        return CURRENCY_FORMAT.format(price);
    }

    private BigDecimal extractMaxPrice(String query) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:dưới|duoi|rẻ hơn|re hon|nhỏ hơn|nho hon|tầm|tam|giá|gia|khoảng)\\s*(\\d+(?:[.,]\\d+)?)\\s*(k|ngàn|ngan|triệu|trieu|đ|vnd)?");
        java.util.regex.Matcher m = p.matcher(query);
        if (m.find()) {
            String numStr = m.group(1).replace(",", ".");
            String unit = m.group(2);
            try {
                double val = Double.parseDouble(numStr);
                if ("k".equalsIgnoreCase(unit) || "ngàn".equalsIgnoreCase(unit) || "ngan".equalsIgnoreCase(unit)) {
                    val *= 1000;
                } else if ("triệu".equalsIgnoreCase(unit) || "trieu".equalsIgnoreCase(unit)) {
                    val *= 1000000;
                } else if (unit == null || unit.isBlank() || "đ".equalsIgnoreCase(unit) || "vnd".equalsIgnoreCase(unit)) {
                    if (val < 1000) {
                        val *= 1000;
                    }
                }
                return BigDecimal.valueOf(val);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private BigDecimal extractMinPrice(String query) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:trên|tren|lớn hơn|lon hon|từ|tu)\\s*(\\d+(?:[.,]\\d+)?)\\s*(k|ngàn|ngan|triệu|trieu|đ|vnd)?");
        java.util.regex.Matcher m = p.matcher(query);
        if (m.find()) {
            String numStr = m.group(1).replace(",", ".");
            String unit = m.group(2);
            try {
                double val = Double.parseDouble(numStr);
                if ("k".equalsIgnoreCase(unit) || "ngàn".equalsIgnoreCase(unit) || "ngan".equalsIgnoreCase(unit)) {
                    val *= 1000;
                } else if ("triệu".equalsIgnoreCase(unit) || "trieu".equalsIgnoreCase(unit)) {
                    val *= 1000000;
                } else if (unit == null || unit.isBlank() || "đ".equalsIgnoreCase(unit) || "vnd".equalsIgnoreCase(unit)) {
                    if (val < 1000) {
                        val *= 1000;
                    }
                }
                return BigDecimal.valueOf(val);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String detectCategorySlug(String query) {
        if (query.contains("sinh nhật") || query.contains("sinh nhat") || query.contains("birthday") || query.contains("tiệc")) {
            return "banh-sinh-nhat";
        }
        if (query.contains("bánh mì") || query.contains("banh mi") || query.contains("croissant")) {
            return "banh-mi";
        }
        if (query.contains("bánh ngọt") || query.contains("banh ngot") || query.contains("mousse") || query.contains("cheesecake")) {
            return "banh-ngot";
        }
        if (query.contains("bánh quy") || query.contains("banh quy") || query.contains("cookie")) {
            return "banh-quy";
        }
        if (query.contains("socola") || query.contains("chocolate") || query.contains("nama")) {
            return "socola";
        }
        if (query.contains("đồ uống") || query.contains("do uong") || query.contains("trà") || query.contains("cà phê") || query.contains("latte")) {
            return "do-uong";
        }
        if (query.contains("set bánh") || query.contains("set banh") || query.contains("hộp quà")) {
            return "set-banh";
        }
        return null;
    }

    private String detectCategoryDisplayName(String query) {
        String slug = detectCategorySlug(query);
        if (slug == null) return null;
        return switch (slug) {
            case "banh-sinh-nhat" -> "Bánh sinh nhật";
            case "banh-mi" -> "Bánh mì";
            case "banh-ngot" -> "Bánh ngọt";
            case "banh-quy" -> "Bánh quy";
            case "socola" -> "Socola";
            case "do-uong" -> "Đồ uống";
            case "set-banh" -> "Set bánh";
            default -> "Sản phẩm";
        };
    }
}

