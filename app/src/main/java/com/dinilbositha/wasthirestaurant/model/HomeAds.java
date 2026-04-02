package com.dinilbositha.wasthirestaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeAds {
    private String imageUrl;
    private String redirectUrl;
    private boolean isVisible;
}
