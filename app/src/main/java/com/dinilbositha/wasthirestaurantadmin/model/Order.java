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
public class Order {
    private String orderId;
    private String customerName;
    private String orderType;
    private String tableNumber;
    private String deliveryAddress;
    private String orderStatus;
    private double total;
    private long createdAt;
    private List<OrderItem> items;
}