package com.rvsfishworld.ui.generic;

import com.rvsfishworld.db.Database;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class GenericDocumentEntryDialog extends CompactCrudDialog {
    private final boolean readOnly;

    public GenericDocumentEntryDialog(Window owner, Map<String, Object> values, boolean readOnly) {
        super(owner, readOnly ? "View Generic Document" : "Edit Generic Document", config(), normalize(values), readOnly);
        this.readOnly = readOnly;
    }

    @Override
    public Map<String, Object> values() {
        Map<String, Object> values = super.values();
        if (readOnly) {
            return values;
        }
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(
                     "UPDATE generic_documents SET document_type = ?, document_no = ?, document_date = STR_TO_DATE(?, '%m/%d/%Y'), party_code = ?, party_name = ?, status = ?, notes = ?, updated_at = CURRENT_TIMESTAMP WHERE document_id = ?")) {
            ps.setString(1, text(values.get("document_type")));
            ps.setString(2, text(values.get("document_no")));
            ps.setString(3, text(values.get("document_date")));
            ps.setString(4, text(values.get("party_code")));
            ps.setString(5, text(values.get("party_name")));
            ps.setString(6, text(values.get("status")));
            ps.setString(7, text(values.get("notes")));
            ps.setLong(8, Long.parseLong(String.valueOf(values.get("document_id"))));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to save generic document: " + e.getMessage(), e);
        }
        return values;
    }

    private static CrudTableConfig config() {
        return new CrudTableConfig("Generic Document")
                .addField(new CrudFieldSpec("document_type", "Document Type", CrudFieldType.TEXT, true))
                .addField(new CrudFieldSpec("document_no", "Document No", CrudFieldType.TEXT, true))
                .addField(new CrudFieldSpec("document_date", "Date", CrudFieldType.DATE, true))
                .addField(new CrudFieldSpec("party_code", "Party Code", CrudFieldType.TEXT, false))
                .addField(new CrudFieldSpec("party_name", "Party Name", CrudFieldType.TEXT, false))
                .addField(new CrudFieldSpec("status", "Status", CrudFieldType.TEXT, false))
                .addField(new CrudFieldSpec("notes", "Notes", CrudFieldType.MULTILINE, false));
    }

    private static Map<String, Object> normalize(Map<String, Object> values) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        if (values != null) {
            normalized.putAll(values);
        }
        normalized.putIfAbsent("document_id", 0L);
        normalized.putIfAbsent("document_type", "");
        normalized.putIfAbsent("document_no", "");
        normalized.putIfAbsent("document_date", "");
        normalized.putIfAbsent("party_code", "");
        normalized.putIfAbsent("party_name", "");
        normalized.putIfAbsent("status", "");
        normalized.putIfAbsent("notes", "");
        return normalized;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
