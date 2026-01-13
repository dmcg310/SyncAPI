package com.syncapi;

import java.util.UUID;

public class TestUtil {
    public static String generateRandomEmail() {
        return generateRandomValue("email") + "@example.com";
    }

    public static String generateRandomName() {
        return generateRandomValue("name");
    }

    public static String generateRandomPasswordHash() {
        return generateRandomValue("hash");
    }

    public static String generateRandomPassword() {
        return generateRandomValue("password");
    }

    public static String generateRandomValue(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    public static Long generateRandomId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }

    public static String generateRandomToken() {
        return generateRandomValue("token");
    }

    public static String generateRandomSecret() {
        return generateRandomValue("secret");
    }
}
