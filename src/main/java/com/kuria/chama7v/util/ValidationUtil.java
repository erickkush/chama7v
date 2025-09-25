package com.kuria.chama7v.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    // Enhanced phone pattern to support multiple Kenyan formats
    private static final Pattern PHONE_PATTERN_07 = Pattern.compile("^07[0-9]{8}$");
    private static final Pattern PHONE_PATTERN_01 = Pattern.compile("^01[0-9]{8}$");
    private static final Pattern PHONE_PATTERN_254 = Pattern.compile("^254[17][0-9]{8}$");
    private static final Pattern PHONE_PATTERN_PLUS254 = Pattern.compile("^\\+254[17][0-9]{8}$");

    private static final Pattern NATIONAL_ID_PATTERN =
            Pattern.compile("^[0-9]{8}$");

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        if (phone == null) return false;

        return PHONE_PATTERN_07.matcher(phone).matches() ||
                PHONE_PATTERN_01.matcher(phone).matches() ||
                PHONE_PATTERN_254.matcher(phone).matches() ||
                PHONE_PATTERN_PLUS254.matcher(phone).matches();
    }

    public boolean isValidNationalId(String nationalId) {
        return nationalId != null && NATIONAL_ID_PATTERN.matcher(nationalId).matches();
    }

    public String formatPhoneNumber(String phone) {
        if (phone == null) return null;

        // Remove any non-digit characters except +
        phone = phone.replaceAll("[^\\d+]", "");

        // Handle different input formats
        if (phone.startsWith("+254")) {
            phone = phone.substring(1); // Remove the +
        } else if (phone.startsWith("07") || phone.startsWith("01")) {
            // Convert 07XXXXXXXX or 01XXXXXXXX to 254XXXXXXX
            phone = "254" + phone.substring(1);
        } else if (phone.startsWith("7") && phone.length() == 9) {
            // Convert 7XXXXXXXX to 254XXXXXXX
            phone = "254" + phone;
        } else if (phone.startsWith("1") && phone.length() == 9) {
            // Convert 1XXXXXXXX to 254XXXXXXX
            phone = "254" + phone;
        }

        // Validate final format
        if (!PHONE_PATTERN_254.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }

        return phone;
    }

    public String generateLoanNumber() {
        java.time.LocalDate now = java.time.LocalDate.now();
        String date = String.format("%d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "LN-" + date + "-" + random;
    }
}