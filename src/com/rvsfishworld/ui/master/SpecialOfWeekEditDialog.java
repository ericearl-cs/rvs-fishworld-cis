package com.rvsfishworld.ui.master;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SpecialOfWeekEditDialog extends FoxProChildDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final JTextField txtProductCode = FoxProTheme.createTextField(14);
    private final JTextField txtSpecialPrice = FoxProTheme.createTextField(12);
    private final JTextField txtStartDate = FoxProTheme.createTextField(10);
    private final JTextField txtEndDate = FoxProTheme.createTextField(10);
    private final JTextField txtRemarks = FoxProTheme.createTextField(28);
    private final JCheckBox chkActive = new JCheckBox("Active");
    private final Long specialWeekId;
    private boolean saved;

    public SpecialOfWeekEditDialog(Window owner, String title, Map<String, Object> values) {
        super(owner, title, 620, 280);
        this.specialWeekId = values.containsKey("special_week_id")
                ? Long.parseLong(String.valueOf(values.get("special_week_id")))
                : null;
        setContentPane(buildContent());
        loadValues(values);
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

        addField(form, gbc, 0, "Product Code", txtProductCode);
        addField(form, gbc, 1, "Special Price", txtSpecialPrice);
        addField(form, gbc, 2, "Start Date", txtStartDate);
        addField(form, gbc, 3, "End Date", txtEndDate);
        addField(form, gbc, 4, "Remarks", txtRemarks);
        gbc.gridx = 1;
        gbc.gridy = 5;
        form.add(chkActive, gbc);

        JPanel actions = new JPanel();
        actions.setBackground(FoxProTheme.PANEL);
        JButton saveButton = FoxProTheme.createButton("Save");
        JButton exitButton = FoxProTheme.createButton("Exit");
        saveButton.addActionListener(e -> save());
        exitButton.addActionListener(e -> dispose());
        actions.add(saveButton);
        actions.add(exitButton);

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

    private void loadValues(Map<String, Object> values) {
        txtProductCode.setText(text(values.get("product_code")));
        txtSpecialPrice.setText(text(values.get("special_price")));
        txtStartDate.setText(text(values.get("start_date")));
        txtEndDate.setText(text(values.get("end_date")));
        txtRemarks.setText(text(values.get("remarks")));
        chkActive.setSelected(!values.containsKey("is_active") || Boolean.parseBoolean(String.valueOf(values.get("is_active"))));
    }

    private void save() {
        String productCode = txtProductCode.getText().trim();
        if (productCode.isBlank()) {
            CisDialogs.showInfo(this, "Product code is required.");
            return;
        }
        BigDecimal specialPrice;
        try {
            specialPrice = new BigDecimal(txtSpecialPrice.getText().trim().isEmpty() ? "0" : txtSpecialPrice.getText().trim());
        } catch (NumberFormatException e) {
            CisDialogs.showError(this, "Invalid special price.");
            return;
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(specialWeekId == null
                     ? "INSERT INTO special_of_week (product_code, special_price, start_date, end_date, remarks, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())"
                     : "UPDATE special_of_week SET product_code=?, special_price=?, start_date=?, end_date=?, remarks=?, is_active=?, updated_at=NOW() WHERE special_week_id=?")) {
            int i = 1;
            ps.setString(i++, productCode);
            ps.setBigDecimal(i++, specialPrice);
            bindDate(ps, i++, txtStartDate.getText().trim());
            bindDate(ps, i++, txtEndDate.getText().trim());
            ps.setString(i++, txtRemarks.getText().trim());
            ps.setBoolean(i++, chkActive.isSelected());
            if (specialWeekId != null) {
                ps.setLong(i, specialWeekId);
            }
            ps.executeUpdate();
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void bindDate(PreparedStatement ps, int index, String value) throws Exception {
        if (value == null || value.isBlank()) {
            ps.setDate(index, null);
        } else {
            ps.setDate(index, java.sql.Date.valueOf(LocalDate.parse(value, DATE_FORMAT)));
        }
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
