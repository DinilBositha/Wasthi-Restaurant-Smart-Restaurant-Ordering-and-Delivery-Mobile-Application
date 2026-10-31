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
public class CartItem {

    private String cartItemId;
    private String productId;
    private String productTitle;
    private String productImage;
    private List<SelectedVariant> selectedVariants;
    private double unitPrice;
    private int quantity;

    // local UI validation flags
    private boolean available;
    private String validationMessage;
    private boolean changed;

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