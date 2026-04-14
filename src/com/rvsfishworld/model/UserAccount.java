package com.rvsfishworld.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UserAccount {
    private long userId;
    private String username;
    private String displayName;
    private String passwordHash;
    private boolean mustResetPassword;
    private String rightsCsv;
    private String rawFlagsCsv;
    private String photoHint;
    private boolean active;
    private LocalDateTime lastLoginAt;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isMustResetPassword() {
        return mustResetPassword;
    }

    public void setMustResetPassword(boolean mustResetPassword) {
        this.mustResetPassword = mustResetPassword;
    }

    public String getRightsCsv() {
        return rightsCsv;
    }

    public void setRightsCsv(String rightsCsv) {
        this.rightsCsv = rightsCsv;
    }

    public String getRawFlagsCsv() {
        return rawFlagsCsv;
    }

    public void setRawFlagsCsv(String rawFlagsCsv) {
        this.rawFlagsCsv = rawFlagsCsv;
    }

    public String getPhotoHint() {
        return photoHint;
    }

    public void setPhotoHint(String photoHint) {
        this.photoHint = photoHint;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isAdministrator() {
        if ("ERIC".equalsIgnoreCase(username)) {
            return true;
        }
        if (containsToken(rightsCsv, "ADMIN") || containsToken(rawFlagsCsv, "ADMIN")) {
            return true;
        }
        return isBlank(rightsCsv) && isBlank(rawFlagsCsv);
    }

    public boolean hasRight(String right) {
        return isAdministrator() || containsToken(rightsCsv, right);
    }

    public boolean hasRawFlag(String flag) {
        return isAdministrator() || containsToken(rawFlagsCsv, flag);
    }

    public Map<String, Boolean> rawFlags() {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (String token : splitTokens(rawFlagsCsv)) {
            flags.put(token.toUpperCase(), true);
        }
        return flags;
    }

    private boolean containsToken(String csv, String expected) {
        return splitTokens(csv).stream().anyMatch(token -> token.equalsIgnoreCase(expected));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private java.util.List<String> splitTokens(String csv) {
        if (csv == null || csv.isBlank()) {
            return java.util.List.of();
        }
        return Arrays.stream(csv.split("[,;\\s]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }
}
