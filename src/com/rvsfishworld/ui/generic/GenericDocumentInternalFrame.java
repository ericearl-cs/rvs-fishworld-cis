package com.rvsfishworld.ui.generic;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.core.CisDialogs;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class GenericDocumentInternalFrame extends DataBrowseInternalFrame {
    public GenericDocumentInternalFrame() {
        super(
                "Generic Document",
                "Generic Document",
                new String[]{"Order by Type", "Order by Document No.", "Order by Date"},
                new String[]{
                        "SELECT document_id, document_type, document_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(status, '') FROM generic_documents ORDER BY document_type, document_date DESC, document_no",
                        "SELECT document_id, document_type, document_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(status, '') FROM generic_documents ORDER BY document_no",
                        "SELECT document_id, document_type, document_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(status, '') FROM generic_documents ORDER BY document_date DESC, document_no"
                },
                new String[]{"Find", "View", "Edit", "Refresh", "Exit"},
                new String[]{"ID", "TYPE", "DOC. NO.", "DATE", "PARTY CODE", "PARTY NAME", "STATUS"},
                "This browse loads live generic document headers from MySQL.");
        var column = getTable().getColumnModel().getColumn(0);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "VIEW" -> openSelected(true);
            case "EDIT" -> openSelected(false);
            default -> super.handleCommand(label);
        }
    }

    private void openSelected(boolean readOnly) {
        Map<String, Object> values = selectedValues();
        if (values.isEmpty()) {
            CisDialogs.showInfo(this, "Select a generic document row first.");
            return;
        }
        GenericDocumentEntryDialog dialog = new GenericDocumentEntryDialog(SwingUtilities.getWindowAncestor(this), values, readOnly);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            dialog.values();
            loadRows();
        }
    }

    private Map<String, Object> selectedValues() {
        int row = getTable().getSelectedRow();
        if (row < 0) {
            return Map.of();
        }
        int modelRow = getTable().convertRowIndexToModel(row);
        long id = Long.parseLong(String.valueOf(getModel().getValueAt(modelRow, 0)));
        Map<String, Object> values = new LinkedHashMap<>();
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT document_id, document_type, document_no, COALESCE(DATE_FORMAT(document_date, '%m/%d/%Y'), ''), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(status, ''), COALESCE(notes, '') FROM generic_documents WHERE document_id = ?")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Map.of();
                }
                values.put("document_id", rs.getLong(1));
                values.put("document_type", rs.getString(2));
                values.put("document_no", rs.getString(3));
                values.put("document_date", rs.getString(4));
                values.put("party_code", rs.getString(5));
                values.put("party_name", rs.getString(6));
                values.put("status", rs.getString(7));
                values.put("notes", rs.getString(8));
                return values;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load generic document: " + e.getMessage(), e);
        }
    }
}
