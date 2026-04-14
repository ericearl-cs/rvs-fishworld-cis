package com.rvsfishworld.db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((raw == null ? "" : raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash password", e);
        }
    }

    public static boolean matches(String raw, String storedHash, String username) {
        String normalizedRaw = raw == null ? "" : raw.trim();
        String normalizedStored = storedHash == null ? "" : storedHash.trim();
        if (normalizedStored.isEmpty()) {
            return normalizedRaw.isEmpty() || normalizedRaw.equalsIgnoreCase(username == null ? "" : username.trim());
        }
        if (normalizedStored.length() == 64 && normalizedStored.matches("[0-9a-fA-F]{64}")) {
            return sha256(normalizedRaw).equalsIgnoreCase(normalizedStored);
        }
        return normalizedStored.equals(normalizedRaw);
    }
}
