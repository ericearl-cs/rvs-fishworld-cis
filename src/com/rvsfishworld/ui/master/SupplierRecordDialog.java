package com.rvsfishworld.ui.master;

import com.rvsfishworld.dao.MasterFileDAO;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisTheme;
import com.rvsfishworld.ui.core.ReceivingUiMetrics;
import com.rvsfishworld.ui.widgets.ReceivingGridView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SupplierRecordDialog extends JDialog {
    private final MasterFileDAO dao = new MasterFileDAO();
    private final Map<String, Object> baseValues = new LinkedHashMap<>();
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private final ReceivingGridView<Object[]> productGrid = new ReceivingGridView<>();
    private boolean saved;

    public SupplierRecordDialog(Window owner, String title, Map<String, Object> values, boolean readOnly) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        if (values != null) {
            baseValues.putAll(values);
        }
        setResizable(false);
        buildUi(readOnly);
        loadSupplierProducts();
    }

    private void buildUi(boolean readOnly) {
        JPanel root = new JPanel(null);
        root.setBackground(CisTheme.PANEL);
        root.setPreferredSize(new Dimension(CisScale.scale(628), CisScale.scale(280)));

        addField(root, "Supplier Code", "supplier_code", 16, 18, 92, 112, readOnly);
        addField(root, "Supplier Name", "supplier_name", 244, 18, 88, 276, readOnly);
        addTextArea(root, "Supplier Add.", "supplier_address", 16, 50, 92, 286, 52, readOnly);
        addField(root, "Contact Person", "contact_person", 16, 112, 92, 160, readOnly);
        addField(root, "Position", "position_title", 298, 112, 62, 126, readOnly);
        addField(root, "Tel. No.", "telephone_no", 16, 142, 92, 160, readOnly);
        addField(root, "Pager No.", "pager_no", 16, 172, 92, 160, readOnly);
        addField(root, "Fax No.", "fax_no", 298, 142, 62, 126, readOnly);
        addField(root, "ZIP CODE", "zip_code", 298, 172, 62, 126, readOnly);

        JCheckBox exempt = new JCheckBox("Exempt in Stop Forever");
        exempt.setBackground(CisTheme.PANEL);
        exempt.setSelected(truthy(baseValues.get("exempt_stop_forever")));
        exempt.setEnabled(!readOnly);
        exempt.setBounds(CisScale.scale(16), CisScale.scale(204), CisScale.scale(180), CisScale.scale(20));
        root.add(exempt);
        inputs.put("exempt_stop_forever", exempt);

        productGrid.setColumns(List.of(
                new ReceivingGridView.Column("CODE", CisScale.scale(70), ReceivingGridView.ALIGN_LEFT),
                new ReceivingGridView.Column("DESCRIPTION", CisScale.scale(208), ReceivingGridView.ALIGN_LEFT)
        ));
        productGrid.setHeaderHeight(CisScale.scale(20));
        productGrid.setRowHeight(CisScale.scale(17));
        productGrid.setBodyFont(ReceivingUiMetrics.CONTROL_FONT);
        productGrid.setHeaderFont(ReceivingUiMetrics.CONTROL_FONT_BOLD);
        productGrid.setFitColumnsToViewport(false);
        productGrid.setTextProvider((row, column) -> row[column] == null ? "" : row[column].toString());
        productGrid.setRowBackgroundProvider((row, selected) -> selected ? new Color(0, 120, 215) : Color.WHITE);
        productGrid.setRowForegroundProvider((row, selected) -> selected ? Color.WHITE : Color.BLACK);
        JScrollPane gridPane = new JScrollPane(productGrid);
        CisTheme.styleGridScrollPane(gridPane);
        gridPane.setBounds(CisScale.scale(332), CisScale.scale(50), CisScale.scale(276), CisScale.scale(170));
        root.add(gridPane);

        if (!readOnly) {
            JButton save = CisTheme.createFormButton("\\<Save", CisScale.scale(84), CisScale.scale(28));
            save.setBounds(CisScale.scale(442), CisScale.scale(236), CisScale.scale(80), CisScale.scale(25));
            save.addActionListener(e -> onSave());
            root.add(save);
            getRootPane().setDefaultButton(save);
        }

        JButton exit = CisTheme.createFormButton("E\\<xit", CisScale.scale(84), CisScale.scale(28));
        exit.setBounds(CisScale.scale(530), CisScale.scale(236), CisScale.scale(80), CisScale.scale(25));
        exit.addActionListener(e -> dispose());
        root.add(exit);

        ReceivingUiMetrics.applyFormFont(root);
        CisTheme.installEnterAsNextField(root);
        setContentPane(root);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addField(JPanel panel, String label, String key, int x, int y, int labelWidth, int inputWidth, boolean readOnly) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(labelWidth), CisScale.scale(18));
        panel.add(jLabel);

        JTextField field = CisTheme.createTextField(Math.max(8, inputWidth / 8));
        field.setText(valueText(key));
        if (readOnly) {
            CisTheme.styleReadOnlyField(field);
        }
        field.setBounds(CisScale.scale(x + labelWidth + 6), CisScale.scale(y - 2), CisScale.scale(inputWidth), CisScale.scale(22));
        panel.add(field);
        inputs.put(key, field);
    }

    private void addTextArea(JPanel panel, String label, String key, int x, int y, int labelWidth, int inputWidth, int inputHeight, boolean readOnly) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(labelWidth), CisScale.scale(18));
        panel.add(jLabel);
        JTextArea area = new JTextArea(valueText(key));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        CisTheme.styleTextArea(area, readOnly);
        JScrollPane pane = new JScrollPane(area);
        CisTheme.styleGridScrollPane(pane);
        pane.setBounds(CisScale.scale(x + labelWidth + 6), CisScale.scale(y - 2), CisScale.scale(inputWidth), CisScale.scale(inputHeight));
        panel.add(pane);
        inputs.put(key, area);
    }

    private String valueText(String key) {
        Object value = baseValues.get(key);
        return value == null ? "" : value.toString();
    }

    private boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? "" : value.toString().trim().toUpperCase();
        return !(text.isBlank() || "0".equals(text) || "FALSE".equals(text) || "F".equals(text) || "N".equals(text));
    }

    private void loadSupplierProducts() {
        List<Object[]> rows = dao.loadSupplierProducts(valueText("supplier_code"));
        productGrid.setRows(rows);
        if (!rows.isEmpty()) {
            productGrid.setSelectedIndex(0);
        }
    }

    private void onSave() {
        if (fieldText("supplier_code").isBlank()) {
            CisDialogs.showInfo(this, "Supplier Code is required.");
            return;
        }
        if (fieldText("supplier_name").isBlank()) {
            CisDialogs.showInfo(this, "Supplier Name is required.");
            return;
        }
        saved = true;
        dispose();
    }

    private String fieldText(String key) {
        return switch (inputs.get(key)) {
            case JTextField field -> field.getText().trim();
            case JTextArea area -> area.getText().trim();
            case JCheckBox box -> box.isSelected() ? "TRUE" : "FALSE";
            case null, default -> "";
        };
    }

    public boolean isSaved() {
        return saved;
    }

    public Map<String, Object> values() {
        Map<String, Object> merged = new LinkedHashMap<>(baseValues);
        for (Map.Entry<String, JComponent> entry : inputs.entrySet()) {
            merged.put(entry.getKey(), readComponentValue(entry.getValue()));
        }
        return merged;
    }

    private Object readComponentValue(JComponent component) {
        return switch (component) {
            case JCheckBox box -> box.isSelected();
            case JTextArea area -> area.getText().trim();
            case JTextField field -> field.getText().trim();
            case null, default -> "";
        };
    }
}
