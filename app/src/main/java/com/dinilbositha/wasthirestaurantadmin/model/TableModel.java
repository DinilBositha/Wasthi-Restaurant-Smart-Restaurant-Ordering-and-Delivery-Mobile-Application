package com.dinilbositha.wasthirestaurantadmin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableModel {
    private String tableNumber;
    private String qrValue;
    private boolean active;
}