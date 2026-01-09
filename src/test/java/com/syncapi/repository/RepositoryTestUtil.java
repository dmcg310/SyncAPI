package com.syncapi.repository;

import java.util.UUID;

public class RepositoryTestUtil {
    public static String generateRandomEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    public static String generateRandomName() {
        return "name-" + UUID.randomUUID();
    }

    public static String generateRandomPasswordHash() {
        return "hash-" + UUID.randomUUID();
    }
}
