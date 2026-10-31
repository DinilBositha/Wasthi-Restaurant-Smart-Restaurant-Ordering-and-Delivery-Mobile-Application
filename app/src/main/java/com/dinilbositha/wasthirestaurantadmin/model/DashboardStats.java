package com.dinilbositha.wasthirestaurantadmin.model;

import java.util.List;
import java.util.Map;

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
    private double totalRevenue;
    private double todayRevenue;
    private int todayOrders;
    private List<ChartDataPoint> last7DaysSales;
    private List<ChartDataPoint> last30DaysSales;
    private Map<String, Integer> statusDistribution;
}
