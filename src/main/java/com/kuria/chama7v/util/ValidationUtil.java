package com.kuria.chama7v.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^254[17][0-9]{8}$");

    private static final Pattern NATIONAL_ID_PATTERN =
            Pattern.compile("^[0-9]{7,10}$");

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public boolean isValidNationalId(String nationalId) {
        return nationalId != null && NATIONAL_ID_PATTERN.matcher(nationalId).matches();
    }

    public String formatPhoneNumber(String phone) {
        if (phone == null) return null;

        // Remove any non-digit characters
        phone = phone.replaceAll("\\D", "");

        // Convert 07XXXXXXXX to 254XXXXXXX
        if (phone.startsWith("07")) {
            phone = "254" + phone.substring(1);
        } else if (phone.startsWith("01")) {
            phone = "254" + phone.substring(1);
        } else if (phone.startsWith("+254")) {
            phone = phone.substring(1);
        }

        return phone;
    }

    public String generateMemberNumber() {
        // Generate member number: format CHA-YYYYMM-XXX
        java.time.LocalDate now = java.time.LocalDate.now();
        String yearMonth = String.format("%d%02d", now.getYear(), now.getMonthValue());
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return "CHA-" + yearMonth + "-" + random;
    }

    public String generateLoanNumber() {
        // Generate loan number: format LN-YYYYMMDD-XXXX
        java.time.LocalDate now = java.time.LocalDate.now();
        String date = String.format("%d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "LN-" + date + "-" + random;
    }
}