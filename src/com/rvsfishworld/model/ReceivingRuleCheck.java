package com.rvsfishworld.model;

public class ReceivingRuleCheck {
    private final boolean valid;
    private final String message;

    public ReceivingRuleCheck(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
