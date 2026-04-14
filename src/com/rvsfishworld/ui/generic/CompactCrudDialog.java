package com.rvsfishworld.ui.generic;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CompactCrudDialog extends FoxProChildDialog {
    private final CrudTableConfig config;
    private final Map<String, Object> baseValues = new LinkedHashMap<>();
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private final boolean readOnly;
    private boolean saved;

    public CompactCrudDialog(Window owner, String title, CrudTableConfig config, Map<String, Object> values, boolean readOnly) {
        super(owner, title, 720, 420);
        this.config = config;
        this.readOnly = readOnly;
        if (values != null) {
            baseValues.putAll(values);
        }
        setContentPane(buildContent());
    }

    public boolean isSaved() {
        return saved;
    }

    public Map<String, Object> values() {
        Map<String, Object> merged = new LinkedHashMap<>(baseValues);
        for (CrudFieldSpec spec : config.getFields()) {
            JComponent input = inputs.get(spec.getKey());
            Object value = switch (spec.getType()) {
                case CHECKBOX -> input instanceof JCheckBox checkBox && checkBox.isSelected();
                case MULTILINE -> input instanceof JTextArea area ? area.getText().trim() : "";
                default -> input instanceof JTextField field ? field.getText().trim() : "";
            };
            merged.put(spec.getKey(), value);
        }
        return merged;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        for (CrudFieldSpec spec : config.getFields()) {
            Component field = createField(spec);
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            form.add(new JLabel(spec.getLabel()), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            form.add(field, gbc);
            row++;
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        if (!readOnly) {
            JButton saveButton = FoxProTheme.createButton("Save");
            saveButton.addActionListener(e -> onSave());
            buttons.add(saveButton);
            getRootPane().setDefaultButton(saveButton);
        }
        JButton exitButton = FoxProTheme.createButton(readOnly ? "Close" : "Exit");
        exitButton.addActionListener(e -> dispose());
        buttons.add(exitButton);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private Component createField(CrudFieldSpec spec) {
        Object value = baseValues.get(spec.getKey());
        return switch (spec.getType()) {
            case CHECKBOX -> {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setBackground(FoxProTheme.PANEL);
                checkBox.setSelected(parseBoolean(value));
                checkBox.setEnabled(!readOnly);
                inputs.put(spec.getKey(), checkBox);
                yield checkBox;
            }
            case MULTILINE -> {
                JTextArea area = new JTextArea(4, 28);
                area.setFont(FoxProTheme.FONT);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setText(text(value));
                area.setEditable(!readOnly);
                inputs.put(spec.getKey(), area);
                yield new javax.swing.JScrollPane(area);
            }
            default -> {
                JTextField field = FoxProTheme.createTextField(spec.getType() == CrudFieldType.NUMBER ? 12 : 24);
                field.setText(text(value));
                field.setEditable(!readOnly);
                inputs.put(spec.getKey(), field);
                yield field;
            }
        };
    }

    private void onSave() {
        List<String> missing = config.getFields().stream()
                .filter(CrudFieldSpec::isRequired)
                .filter(spec -> isBlank(spec, inputs.get(spec.getKey())))
                .map(CrudFieldSpec::getLabel)
                .toList();
        if (!missing.isEmpty()) {
            CisDialogs.showInfo(this, "Required field is blank: " + missing.get(0));
            return;
        }
        saved = true;
        dispose();
    }

    private boolean isBlank(CrudFieldSpec spec, JComponent input) {
        return switch (spec.getType()) {
            case CHECKBOX -> false;
            case MULTILINE -> !(input instanceof JTextArea area) || area.getText().trim().isBlank();
            default -> !(input instanceof JTextField field) || field.getText().trim().isBlank();
        };
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && ("1".equals(String.valueOf(value)) || "YES".equalsIgnoreCase(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value)));
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
