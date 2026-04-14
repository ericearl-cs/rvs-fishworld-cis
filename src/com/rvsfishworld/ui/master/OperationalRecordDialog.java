package com.rvsfishworld.ui.master;

import com.rvsfishworld.dao.OperationalTableDAO;
import com.rvsfishworld.ui.generic.CompactCrudDialog;
import com.rvsfishworld.ui.generic.CrudFieldSpec;
import com.rvsfishworld.ui.generic.CrudFieldType;
import com.rvsfishworld.ui.generic.CrudTableConfig;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class OperationalRecordDialog extends CompactCrudDialog {
    public OperationalRecordDialog(Window owner, String kind, Map<String, Object> values, boolean readOnly) {
        super(owner, titleFor(kind), configFor(kind), normalize(kind, values), readOnly);
    }

    private static String titleFor(String kind) {
        return switch ((kind == null ? "" : kind).toUpperCase()) {
            case "AREA" -> "Area Record";
            case "BANK" -> "Bank Record";
            default -> "Salesman Record";
        };
    }

    private static CrudTableConfig configFor(String kind) {
        OperationalTableDAO.CrudConfig daoConfig = new OperationalTableDAO().crudConfig(kind);
        CrudTableConfig config = new CrudTableConfig(daoConfig.title());
        config.addField(new CrudFieldSpec(daoConfig.codeKey(), "Code", CrudFieldType.TEXT, true));
        config.addField(new CrudFieldSpec(daoConfig.nameKey(), "Name", CrudFieldType.TEXT, true));
        if (!daoConfig.extraKey().isBlank()) {
            config.addField(new CrudFieldSpec(daoConfig.extraKey(), daoConfig.extraLabel(), CrudFieldType.TEXT, false));
        }
        config.addField(new CrudFieldSpec("is_active", "Active", CrudFieldType.CHECKBOX, false));
        return config;
    }

    private static Map<String, Object> normalize(String kind, Map<String, Object> values) {
        OperationalTableDAO.CrudConfig daoConfig = new OperationalTableDAO().crudConfig(kind);
        Map<String, Object> normalized = new LinkedHashMap<>();
        if (values != null) {
            normalized.putAll(values);
        }
        normalized.putIfAbsent(daoConfig.idKey(), "");
        normalized.putIfAbsent(daoConfig.codeKey(), "");
        normalized.putIfAbsent(daoConfig.nameKey(), "");
        if (!daoConfig.extraKey().isBlank()) {
            normalized.putIfAbsent(daoConfig.extraKey(), "");
        }
        normalized.putIfAbsent("is_active", true);
        return normalized;
    }
}
