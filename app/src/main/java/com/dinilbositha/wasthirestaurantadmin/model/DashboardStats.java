package com.dinilbositha.wasthirestaurantadmin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private int totalOrders;
    private int pendingOrders;
    private int productsCount;
    private int usersCount;
}
