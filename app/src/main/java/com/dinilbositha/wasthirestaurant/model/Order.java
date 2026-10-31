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
public class Order {

    private String orderId;
    private String userId;
    private String customerName;
    private String customerEmail;

    private String orderType;       // TABLE_ORDER / DELIVERY
    private String tableNumber;     // if table order
    private String deliveryAddress; // if delivery
    private double latitude;
    private double longitude;

    private String paymentStatus;   // Paid
    private String orderStatus;     // Pending / Preparing / Ready / Delivered

    private double subtotal;
    private double tax;
    private double deliveryFee;
    private double total;

    private List<CartItem> items;

    private long createdAt;

}