package com.rvsfishworld.ui.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrudTableConfig {
    private final String title;
    private final List<CrudFieldSpec> fields = new ArrayList<>();

    public CrudTableConfig(String title) {
        this.title = title;
    }

    public CrudTableConfig addField(CrudFieldSpec spec) {
        fields.add(spec);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public List<CrudFieldSpec> getFields() {
        return Collections.unmodifiableList(fields);
    }
}
