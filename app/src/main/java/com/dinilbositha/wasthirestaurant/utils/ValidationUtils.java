package com.dinilbositha.wasthirestaurant.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[A-Z])" +
                    "(?=.*[a-z])" +
                    "(?=\\S+$)" +
                    ".{8,}" +
                    "$");
    public static Boolean isValidatePassword(String password){
     return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static Boolean isValidateMobile(String mobile){
        return mobile != null && mobile.matches("^07[0-9]{8}$");
    }
}
