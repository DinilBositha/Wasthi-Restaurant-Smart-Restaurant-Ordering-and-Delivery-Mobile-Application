package com.dinilbositha.wasthirestaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartValidationResult {
    private CartItem cartItem;
    private boolean valid;
    private boolean changed;
    private String message;
}