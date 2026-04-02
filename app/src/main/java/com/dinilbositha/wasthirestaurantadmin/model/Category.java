package com.dinilbositha.wasthirestaurantadmin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private String categoryId;
    private String categoryName;
    private String imgUrl;
    private boolean status;
}