package com.dinilbositha.wasthirestaurantadmin.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String productId;
    private String productTitle;
    private String productImage;
    private List<SelectedVariant> selectedVariants;
    private double unitPrice;
    private int quantity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedVariant {
        private String type;
        private String name;
        private double extraPrice;
    }
}