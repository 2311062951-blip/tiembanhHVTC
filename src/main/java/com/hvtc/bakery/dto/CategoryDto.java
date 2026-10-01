package com.hvtc.bakery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String slug;
    private String description;
    private String image;
    private boolean status = true;

    private MultipartFile imageFile;
}
