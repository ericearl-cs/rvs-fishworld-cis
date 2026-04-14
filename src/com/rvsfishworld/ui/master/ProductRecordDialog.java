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
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ProductRecordDialog extends FoxProChildDialog {
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private final Map<String, Object> baseValues = new LinkedHashMap<>();
    private boolean saved;

    public ProductRecordDialog(Window owner) {
        this(owner, "Product Record", Map.of(), false);
    }

    public ProductRecordDialog(Window owner, String title, Map<String, Object> values, boolean readOnly) {
        super(owner, title, 900, 460);
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

        addField(form, gbc, 0, 0, "Product Code", "product_code", 10, readOnly);
        addField(form, gbc, 0, 3, "Description", "description", 26, readOnly);
        addField(form, gbc, 1, 0, "Scientific Name", "scientific_name", 24, readOnly);
        addField(form, gbc, 1, 3, "Category", "category_code", 8, readOnly);
        addField(form, gbc, 2, 0, "U/M", "unit_of_measure", 8, readOnly);
        addField(form, gbc, 2, 3, "Total Qty", "total_quantity", 8, readOnly);
        addField(form, gbc, 3, 0, "Previous Cost", "previous_cost", 8, readOnly);
        addField(form, gbc, 3, 3, "Average Cost", "average_cost", 8, readOnly);
        addField(form, gbc, 4, 0, "Price A", "price_a", 8, readOnly);
        addField(form, gbc, 4, 2, "Price B", "price_b", 8, readOnly);
        addField(form, gbc, 4, 4, "Price C", "price_c", 8, readOnly);
        addField(form, gbc, 5, 0, "Price D", "price_d", 8, readOnly);
        addField(form, gbc, 5, 2, "Price E", "price_e", 8, readOnly);
        addField(form, gbc, 5, 4, "Price F", "price_f", 8, readOnly);
        addField(form, gbc, 6, 0, "Price G", "price_g", 8, readOnly);
        addField(form, gbc, 6, 2, "Special", "special_price", 8, readOnly);
        addField(form, gbc, 6, 4, "Local Sales", "local_sales_price", 8, readOnly);

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

    private void onSave() {
        if (fieldText("product_code").isBlank()) {
            CisDialogs.showInfo(this, "Product Code is required.");
            return;
        }
        if (fieldText("description").isBlank()) {
            CisDialogs.showInfo(this, "Description is required.");
            return;
        }
        saved = true;
        dispose();
    }

    private String fieldText(String key) {
        JComponent component = inputs.get(key);
        return component instanceof JTextField field ? field.getText().trim() : "";
    }

    private String stringValue(String key) {
        Object value = baseValues.get(key);
        return value == null ? "" : value.toString();
    }
}
