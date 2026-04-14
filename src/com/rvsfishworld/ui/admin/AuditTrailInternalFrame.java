package com.rvsfishworld.ui.admin;

import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;

@SuppressWarnings("serial")
public class AuditTrailInternalFrame extends DataBrowseInternalFrame {
    public AuditTrailInternalFrame() {
        super(
                "Audit Trail",
                "Audit Trail",
                new String[]{"Order by Time", "Order by User", "Order by Module"},
                new String[]{
                        "SELECT DATE_FORMAT(event_time, '%m/%d/%Y %H:%i:%s'), COALESCE(username, ''), action_type, module_name, COALESCE(reference_no, ''), LEFT(COALESCE(details, ''), 120) FROM audit_events ORDER BY event_time DESC",
                        "SELECT DATE_FORMAT(event_time, '%m/%d/%Y %H:%i:%s'), COALESCE(username, ''), action_type, module_name, COALESCE(reference_no, ''), LEFT(COALESCE(details, ''), 120) FROM audit_events ORDER BY username, event_time DESC",
                        "SELECT DATE_FORMAT(event_time, '%m/%d/%Y %H:%i:%s'), COALESCE(username, ''), action_type, module_name, COALESCE(reference_no, ''), LEFT(COALESCE(details, ''), 120) FROM audit_events ORDER BY module_name, event_time DESC"
                },
                new String[]{"Find", "Refresh", "Exit"},
                new String[]{"TIME", "USER", "ACTION", "MODULE", "REF", "DETAILS"},
                "This browse loads live audit rows from MySQL.");
    }
}
