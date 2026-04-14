package com.rvsfishworld.ui.generic;

public class CrudFieldSpec {
    private final String key;
    private final String label;
    private final CrudFieldType type;
    private final boolean required;

    public CrudFieldSpec(String key, String label, CrudFieldType type, boolean required) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public CrudFieldType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
}
