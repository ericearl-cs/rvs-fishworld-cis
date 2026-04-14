package com.rvsfishworld.ui.admin;

import com.rvsfishworld.dao.AppUserDAO;
import com.rvsfishworld.model.UserAccount;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class UserAccountDialog extends FoxProChildDialog {
    private final AppUserDAO userDao = new AppUserDAO();
    private final UserAccount user;
    private final boolean readOnly;
    private boolean saved;

    private final JTextField txtUsername = FoxProTheme.createTextField(18);
    private final JTextField txtDisplayName = FoxProTheme.createTextField(24);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JTextField txtRights = FoxProTheme.createTextField(36);
    private final JTextField txtFlags = FoxProTheme.createTextField(36);
    private final JCheckBox chkActive = new JCheckBox("Active");
    private final JCheckBox chkMustReset = new JCheckBox("Must Reset Password");

    public UserAccountDialog(Window owner, String title, UserAccount user, boolean readOnly) {
        super(owner, title, 680, 340);
        this.user = user == null ? new UserAccount() : user;
        this.readOnly = readOnly;
        setContentPane(buildContent());
        loadValues();
    }

    public boolean isSaved() {
        return saved;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, "User Name", txtUsername);
        addField(form, gbc, 1, "Display Name", txtDisplayName);
        addField(form, gbc, 2, "Password", txtPassword);
        addField(form, gbc, 3, "Rights CSV", txtRights);
        addField(form, gbc, 4, "Raw Flags CSV", txtFlags);

        gbc.gridx = 1;
        gbc.gridy = 5;
        form.add(chkActive, gbc);
        gbc.gridy = 6;
        form.add(chkMustReset, gbc);

        JPanel actions = new JPanel();
        actions.setBackground(FoxProTheme.PANEL);
        JButton saveButton = FoxProTheme.createButton("Save");
        JButton closeButton = FoxProTheme.createButton(readOnly ? "Close" : "Exit");
        saveButton.addActionListener(e -> save());
        closeButton.addActionListener(e -> dispose());
        actions.add(saveButton);
        actions.add(closeButton);

        if (readOnly) {
            saveButton.setEnabled(false);
        }

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void loadValues() {
        txtUsername.setText(nullToEmpty(user.getUsername()));
        txtDisplayName.setText(nullToEmpty(user.getDisplayName()));
        txtRights.setText(nullToEmpty(user.getRightsCsv()));
        txtFlags.setText(nullToEmpty(user.getRawFlagsCsv()));
        chkActive.setSelected(user.getUserId() <= 0 || user.isActive());
        chkMustReset.setSelected(user.isMustResetPassword());

        boolean editable = !readOnly;
        txtUsername.setEditable(editable);
        txtDisplayName.setEditable(editable);
        txtPassword.setEditable(editable);
        txtRights.setEditable(editable);
        txtFlags.setEditable(editable);
        chkActive.setEnabled(editable);
        chkMustReset.setEnabled(editable);
    }

    private void save() {
        String username = txtUsername.getText().trim();
        if (username.isBlank()) {
            CisDialogs.showInfo(this, "User name is required.");
            return;
        }
        user.setUsername(username);
        user.setDisplayName(txtDisplayName.getText().trim());
        user.setRightsCsv(txtRights.getText().trim());
        user.setRawFlagsCsv(txtFlags.getText().trim());
        user.setActive(chkActive.isSelected());
        user.setMustResetPassword(chkMustReset.isSelected());
        try {
            userDao.save(user, new String(txtPassword.getPassword()));
            saved = true;
            dispose();
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
