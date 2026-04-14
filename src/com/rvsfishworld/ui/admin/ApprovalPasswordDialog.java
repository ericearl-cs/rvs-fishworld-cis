package com.rvsfishworld.ui.admin;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

@SuppressWarnings("serial")
public class ApprovalPasswordDialog extends FoxProChildDialog {
    private final JPasswordField passwordField = new JPasswordField(18);
    private boolean approved;

    public ApprovalPasswordDialog(Window owner) {
        super(owner, "Approval Password", CisScale.scale(430), CisScale.scale(190));
        setResizable(false);
        setContentPane(buildContent());
    }

    public boolean isApproved() {
        return approved;
    }

    private JPanel buildContent() {
        passwordField.setFont(FoxProTheme.FONT);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        passwordField.setPreferredSize(new Dimension(CisScale.scale(210), CisScale.scale(26)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(CisScale.scale(14), CisScale.scale(16), CisScale.scale(14), CisScale.scale(16))));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(CisScale.scale(8), CisScale.scale(8), CisScale.scale(8), CisScale.scale(8));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(label("Password"), gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JLabel hint = new JLabel("Approval is required for this action.");
        hint.setFont(FoxProTheme.FONT);
        gbc.gridx = 1;
        gbc.gridy = 1;
        form.add(hint, gbc);

        JButton ok = FoxProTheme.createButton("OK");
        JButton cancel = FoxProTheme.createButton("Cancel");
        ok.addActionListener(e -> onApprove());
        cancel.addActionListener(e -> dispose());
        passwordField.addActionListener(e -> onApprove());

        JPanel actions = new JPanel();
        actions.setBackground(FoxProTheme.PANEL);
        actions.add(ok);
        actions.add(cancel);

        JPanel root = new JPanel(new BorderLayout(CisScale.scale(8), CisScale.scale(8)));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(
                CisScale.scale(10), CisScale.scale(10), CisScale.scale(10), CisScale.scale(10)));
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        return root;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(FoxProTheme.FONT_BOLD);
        return label;
    }

    private void onApprove() {
        String entered = new String(passwordField.getPassword()).trim();
        String sql = "SELECT 1 FROM approval WHERE UPPER(pw) = UPPER(?) OR UPPER(pw_user) = UPPER(?) LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entered);
            ps.setString(2, entered);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    approved = true;
                    dispose();
                    return;
                }
            }
            CisDialogs.showError(this, "Invalid approval password.");
        } catch (Exception e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }
}
