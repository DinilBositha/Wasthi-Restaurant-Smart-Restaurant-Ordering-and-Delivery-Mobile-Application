package com.dinilbositha.wasthirestaurant.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private String productId;
    private String productTitle;
    private String description;
    private String categoryId;
    private String categoryName;
    private List<String> productImage;
    private double basePrice;
    private boolean status;

    private List<ProductVariant> productVariants;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductVariant {
        private String variantName;
        private String type;
        private double productPrice;
        private int stockCount;
        private boolean variantStatus;

        // new fields
        private boolean required;
        private boolean multiSelect;
    }
}