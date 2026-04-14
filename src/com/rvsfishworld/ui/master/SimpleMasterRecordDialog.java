package com.rvsfishworld.ui.master;

import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SimpleMasterRecordDialog extends FoxProChildDialog {
    private final List<String> requiredKeys;
    private final Map<String, Object> baseValues = new LinkedHashMap<>();
    private final Map<String, JTextField> inputs = new LinkedHashMap<>();
    private boolean saved;

    public SimpleMasterRecordDialog(Window owner, String title, Map<String, Object> values, boolean readOnly, List<String> fieldOrder, Map<String, String> labels, List<String> requiredKeys) {
        super(owner, title, 620, 320);
        this.requiredKeys = requiredKeys == null ? List.of() : List.copyOf(requiredKeys);
        if (values != null) {
            baseValues.putAll(values);
        }
        setContentPane(buildContent(readOnly, fieldOrder, labels));
    }

    public boolean isSaved() {
        return saved;
    }

    public Map<String, Object> values() {
        Map<String, Object> merged = new LinkedHashMap<>(baseValues);
        for (Map.Entry<String, JTextField> entry : inputs.entrySet()) {
            merged.put(entry.getKey(), entry.getValue().getText().trim());
        }
        return merged;
    }

    private JPanel buildContent(boolean readOnly, List<String> fieldOrder, Map<String, String> labels) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(null);
        form.setBackground(FoxProTheme.PANEL);

        int y = 10;
        for (String key : fieldOrder) {
            JLabel label = new JLabel(labels.getOrDefault(key, key));
            label.setBounds(20, y, 150, 20);
            form.add(label);

            JTextField field = FoxProTheme.createTextField(24);
            field.setText(text(baseValues.get(key)));
            field.setEditable(!readOnly);
            field.setBounds(180, y - 2, 320, 24);
            form.add(field);
            inputs.put(key, field);
            y += 34;
        }

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

    private void onSave() {
        for (String key : requiredKeys) {
            JTextField field = inputs.get(key);
            if (field != null && field.getText().trim().isBlank()) {
                CisDialogs.showInfo(this, "Required field is blank: " + key);
                return;
            }
        }
        saved = true;
        dispose();
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
