package com.rvsfishworld.ui.admin;

import com.rvsfishworld.dao.CompanyProfileDAO;
import com.rvsfishworld.model.CompanyProfile;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CompanyProfileDialog extends FoxProChildDialog {
    public CompanyProfileDialog(Window owner) {
        super(owner, "Company Profile", CisScale.scale(640), CisScale.scale(320));
        setResizable(false);
        setContentPane(buildContent(new CompanyProfileDAO().load()));
    }

    private JPanel buildContent(CompanyProfile profile) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(CisScale.scale(14), CisScale.scale(16), CisScale.scale(14), CisScale.scale(16))));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(CisScale.scale(6), CisScale.scale(8), CisScale.scale(6), CisScale.scale(8));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Company", value(profile.getCompanyName()));
        addRow(form, gbc, 1, "Address", value(profile.getCompanyAddress()));
        addRow(form, gbc, 2, "Executive", value(profile.getExecutiveName()));
        addRow(form, gbc, 3, "Phone", value(profile.getPhone()));
        addRow(form, gbc, 4, "Fax", value(profile.getFax()));
        addRow(form, gbc, 5, "Email", value(profile.getEmail()));
        addRow(form, gbc, 6, "TIN", value(profile.getTin()));
        addRow(form, gbc, 7, "SSS No.", value(profile.getSssNo()));

        JButton close = FoxProTheme.createButton("Close");
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel();
        actions.setBackground(FoxProTheme.PANEL);
        actions.add(close);

        JPanel root = new JPanel(new BorderLayout(CisScale.scale(8), CisScale.scale(8)));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(
                CisScale.scale(10), CisScale.scale(10), CisScale.scale(10), CisScale.scale(10)));
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String labelText, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText + ":");
        label.setFont(FoxProTheme.FONT_BOLD);
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField field = FoxProTheme.createTextField(32);
        field.setText(value);
        field.setEditable(false);
        field.setBackground(Color.WHITE);
        form.add(field, gbc);
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
