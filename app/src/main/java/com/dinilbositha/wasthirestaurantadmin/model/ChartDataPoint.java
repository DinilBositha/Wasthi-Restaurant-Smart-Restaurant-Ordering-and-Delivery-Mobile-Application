package com.dinilbositha.wasthirestaurantadmin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPoint {
    private String label;
    private String fullDate;
    private double revenue;
    private int orderCount;
}
