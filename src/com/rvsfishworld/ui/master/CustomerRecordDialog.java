package com.rvsfishworld.ui.master;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CustomerRecordDialog extends FoxProChildDialog {
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private final Map<String, Object> baseValues = new LinkedHashMap<>();
    private boolean saved;

    public CustomerRecordDialog(Window owner) {
        this(owner, "Customer Record", Map.of(), false);
    }

    public CustomerRecordDialog(Window owner, String title, Map<String, Object> values, boolean readOnly) {
        super(owner, title, 880, 460);
        if (values != null) {
            baseValues.putAll(values);
        }
        setContentPane(buildContent(readOnly));
    }

    public boolean isSaved() {
        return saved;
    }

    public Map<String, Object> values() {
        Map<String, Object> merged = new LinkedHashMap<>(baseValues);
        for (Map.Entry<String, JComponent> entry : inputs.entrySet()) {
            if (entry.getValue() instanceof JTextField field) {
                merged.put(entry.getKey(), field.getText().trim());
            } else if (entry.getValue() instanceof JTextArea area) {
                merged.put(entry.getKey(), area.getText().trim());
            }
        }
        return merged;
    }

    private JPanel buildContent(boolean readOnly) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, 0, "Customer Code", "customer_code", 10, readOnly);
        addField(form, gbc, 0, 3, "Customer Name", "customer_name", 26, readOnly);
        addArea(form, gbc, 1, 0, "Address", "customer_address", 32, 3, readOnly);
        addField(form, gbc, 4, 0, "Discount %", "discount_percent", 8, readOnly);
        addField(form, gbc, 4, 3, "Salesman", "salesman_code", 8, readOnly);
        addField(form, gbc, 5, 0, "Contact Person", "contact_person", 20, readOnly);
        addField(form, gbc, 5, 3, "Position", "position_title", 18, readOnly);
        addField(form, gbc, 6, 0, "Telephone", "telephone_no", 16, readOnly);
        addField(form, gbc, 6, 3, "Fax", "fax_no", 16, readOnly);
        addField(form, gbc, 7, 0, "Terms Days", "terms_days", 8, readOnly);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        if (!readOnly) {
            JButton save = FoxProTheme.createButton("Save");
            save.addActionListener(e -> onSave());
            buttons.add(save);
            getRootPane().setDefaultButton(save);
        }
        JButton exit = FoxProTheme.createButton("Exit");
        exit.addActionListener(e -> dispose());
        buttons.add(exit);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, String key, int columns, boolean readOnly) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = col + 1;
        gbc.weightx = 1;
        JTextField field = FoxProTheme.createTextField(columns);
        field.setText(stringValue(key));
        field.setEditable(!readOnly);
        panel.add(field, gbc);
        inputs.put(key, field);
    }

    private void addArea(JPanel panel, GridBagConstraints gbc, int row, int col, String label, String key, int columns, int rows, boolean readOnly) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = col + 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        JTextArea area = new JTextArea(rows, columns);
        area.setFont(FoxProTheme.FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(stringValue(key));
        area.setEditable(!readOnly);
        panel.add(new javax.swing.JScrollPane(area), gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputs.put(key, area);
    }

    private void onSave() {
        if (fieldText("customer_code").isBlank()) {
            CisDialogs.showInfo(this, "Customer Code is required.");
            return;
        }
        if (fieldText("customer_name").isBlank()) {
            CisDialogs.showInfo(this, "Customer Name is required.");
            return;
        }
        saved = true;
        dispose();
    }

    private String fieldText(String key) {
        JComponent component = inputs.get(key);
        if (component instanceof JTextField field) {
            return field.getText().trim();
        }
        if (component instanceof JTextArea area) {
            return area.getText().trim();
        }
        return "";
    }

    private String stringValue(String key) {
        Object value = baseValues.get(key);
        return value == null ? "" : value.toString();
    }
}
