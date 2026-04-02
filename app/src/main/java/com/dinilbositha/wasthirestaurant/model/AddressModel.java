package com.dinilbositha.wasthirestaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressModel {

    private String id;
    private String title;
    private String fullName;
    private String mobileNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private Double latitude;
    private Double longitude;

    public String getDisplayAddress() {
        StringBuilder builder = new StringBuilder();

        if (addressLine1 != null && !addressLine1.trim().isEmpty()) {
            builder.append(addressLine1);
        }

        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(addressLine2);
        }

        if (city != null && !city.trim().isEmpty()) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(city);
        }

        return builder.toString();
    }

    public String getFormattedAddress() {
        StringBuilder builder = new StringBuilder();

        if (title != null && !title.trim().isEmpty()) {
            builder.append(title).append("\n");
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            builder.append(fullName).append("\n");
        }

        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            builder.append(mobileNumber).append("\n");
        }

        builder.append(getDisplayAddress());

        return builder.toString();
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}