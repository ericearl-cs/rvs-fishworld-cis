package com.rvsfishworld.ui.admin;

import java.awt.Window;

@SuppressWarnings("serial")
public class AdminApprovalPromptDialog extends ApprovalPasswordDialog {
    public AdminApprovalPromptDialog(Window owner) {
        super(owner);
        setTitle("Administrator Approval");
    }
}
