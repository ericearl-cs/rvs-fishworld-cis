package com.rvsfishworld.ui.admin;

import com.rvsfishworld.dao.AppUserDAO;
import com.rvsfishworld.model.UserAccount;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class UserRightsInternalFrame extends DataBrowseInternalFrame {
    private final AppUserDAO userDao = new AppUserDAO();

    public UserRightsInternalFrame() {
        super(
                "User Rights",
                "User Rights",
                new String[]{"Order by User Name", "Order by Display Name"},
                new String[]{
                        "SELECT username, display_name, COALESCE(rights_csv, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM app_users ORDER BY username",
                        "SELECT username, display_name, COALESCE(rights_csv, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM app_users ORDER BY display_name, username"
                },
                new String[]{"Find", "View", "Add", "Edit", "Refresh", "Exit"},
                new String[]{"USER NAME", "DISPLAY NAME", "RIGHTS", "ACTIVE"},
                "This browse loads live app users from MySQL.");
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add User", new UserAccount(), false);
            case "VIEW" -> openSelected("View User", true);
            case "EDIT" -> openSelected("Edit User", false);
            default -> super.handleCommand(label);
        }
    }

    private void openSelected(String title, boolean readOnly) {
        UserAccount user = selectedUser();
        if (user == null) {
            CisDialogs.showInfo(this, "Select a user first.");
            return;
        }
        openDialog(title, user, readOnly);
    }

    private void openDialog(String title, UserAccount user, boolean readOnly) {
        UserAccountDialog dialog = new UserAccountDialog(SwingUtilities.getWindowAncestor(this), title, user, readOnly);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private UserAccount selectedUser() {
        int row = getTable().getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = getTable().convertRowIndexToModel(row);
        String username = String.valueOf(getModel().getValueAt(modelRow, 0));
        return userDao.findByUsername(username);
    }
}
