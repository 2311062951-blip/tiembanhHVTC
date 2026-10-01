package com.hvtc.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String reply;
    private List<ProductSummaryDto> suggestedProducts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductSummaryDto {
        private String name;
        private String slug;
        private String image;
        private String priceFormatted;
    }
}
