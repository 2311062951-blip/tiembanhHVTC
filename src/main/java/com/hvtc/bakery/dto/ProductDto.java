package com.hvtc.bakery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String productCode;

    @NotBlank(message = "Tên bánh không được để trống")
    private String name;

    private String slug;

    private String description;
    private String shortDescription;

    @NotNull(message = "Giá bánh không được để trống")
    @Min(value = 0, message = "Giá bánh phải từ 0đ trở lên")
    private BigDecimal price;

    private BigDecimal salePrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity;

    private String image;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private boolean status = true;
    private boolean featured = false;
    private boolean bestSeller = false;

    private MultipartFile imageFile;
}
