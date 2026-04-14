package com.rvsfishworld.model;

public class LookupItem {
    private long id;
    private String code;
    private String name;
    private String extra;

    public LookupItem(long id, String code, String name) {
        this(id, code, name, "");
    }

    public LookupItem(long id, String code, String name, String extra) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.extra = extra;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getExtra() {
        return extra;
    }
}
